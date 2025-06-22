#include "VideoHandler.h"
#include "AmebaFatFS.h"
#include "mmf2_module.h" // Include for MMF types

VideoHandler::VideoHandler() 
  : configV(VIDEO_CHANNEL), 
    configA(AUDIO_PRESET)
{
  audioStreamer = nullptr;
  avMixStreamer = nullptr;
  
  initialized = false;
  recording = false;
  uploadVideo = true;       // Default to uploading videos
  recordingStartTime = 0;
  recordingDuration = 0;
  videoFilePath = "";
  baseFileName = "VIDEO";
}

VideoHandler::~VideoHandler() {
  // Ensure everything is cleaned up
  end();
}

bool VideoHandler::begin() {
  if (initialized) {
    Serial.println("VideoHandler already initialized.");
    return true;
  }
  
  Serial.println("Initializing VideoHandler components...");
  
  // Initialize SD card using AmebaFatFS member variable
  Serial.println("Attempting to mount SD card...");
  if (!fatfs.begin()) { // Use the member variable fatfs
    Serial.println("[Error] SD card initialization failed!");
    return false;
  }
  Serial.println("SD card initialized and mounted.");
  // !!!!! Do NOT call fatfs.end() here; keep it mounted
  
  // --- Initialize Core Components ONCE ---
  Serial.println("Configuring video channel settings...");
  Camera.configVideoChannel(VIDEO_CHANNEL, configV);
  
  Serial.println("Configuring audio peripheral settings...");
  audio.configAudio(configA);
  
  Serial.println("Configuring AAC encoder settings...");
  aac.configAudio(configA);
  
  // Initialize but don't start components yet
  Serial.println("Initializing video...");
  Camera.videoInit();
  delay(200);
  
  Serial.println("Initializing audio...");
  audio.begin();
  delay(100);
  
  Serial.println("Initializing AAC encoder...");
  aac.begin();
  delay(100);
  
  // Create StreamIO objects
  createStreamers();
  
  Serial.println("VideoHandler components initialized successfully.");
  initialized = true;
  return true;
}

void VideoHandler::end() {
  // Stop recording if active
  if (recording) {
    stopRecording();
  }
  
  // Clean up StreamIO objects
  destroyStreamers();
  
  // End main components
  audio.end();
  aac.end();
  Camera.videoDeinit();
  
  // End the filesystem
  fatfs.end();
  Serial.println("SD card unmounted.");
  
  initialized = false;
}

void VideoHandler::createStreamers() {
  Serial.println("Creating StreamIO objects...");
  // Create new StreamIO objects
  if (audioStreamer == nullptr) {
    audioStreamer = new StreamIO(1, 1);  // 1 Input Audio -> 1 Output AAC
  }
  
  if (avMixStreamer == nullptr) {
    avMixStreamer = new StreamIO(2, 1);  // 2 Input Video + Audio -> 1 Output MP4
  }
}

void VideoHandler::destroyStreamers() {
  Serial.println("Destroying StreamIO objects...");
  // Clean up and delete StreamIO objects
  if (audioStreamer != nullptr) {
    delete audioStreamer;
    audioStreamer = nullptr;
  }
  
  if (avMixStreamer != nullptr) {
    delete avMixStreamer;
    avMixStreamer = nullptr;
  }
}

void VideoHandler::resetStreamConnections() {
  Serial.println("Resetting stream connections...");
  
  // Recreate StreamIO objects to ensure clean state
  destroyStreamers();
  createStreamers();
  
  // Register audio stream connections
  audioStreamer->registerInput(audio);
  audioStreamer->registerOutput(aac);
  
  // Make sure Camera hardware is in clean state by reconfiguring it
  Serial.println("Resetting video hardware state...");
  
  // Just reconfigure the video channel without reinitializing
  // This is safer than reinitializing the entire video subsystem
  Camera.configVideoChannel(VIDEO_CHANNEL, configV);
  delay(300); // Give time for configuration to apply
}

bool VideoHandler::startRecording(unsigned long durationMs, bool shouldUpload) {
  Serial.println("==> startRecording called");
  if (!initialized) {
      Serial.println("[Error] VideoHandler not initialized. Call begin() first.");
      return false;
  }
  if (recording) {
    Serial.println("Video recording already in progress!");
    return false;
  }
  
  Serial.println("Attempting to start video recording...");
  
  // Reset all stream connections to ensure clean state
  resetStreamConnections();
  
  // Convert duration from milliseconds to seconds for MP4Recording
  int durationSeconds = durationMs / 1000;
  if (durationSeconds < 1) {
    durationSeconds = 1; // Minimum 1 second
  }
  
  // Set the upload flag based on parameter
  uploadVideo = shouldUpload;
  if (!uploadVideo) {
    Serial.println("Note: Video will be saved to SD card only (no upload)");
  }
  
  // Generate the filename for this recording
  String filename = generateFilename();
  videoFilePath = "0:/" + filename + ".mp4"; // FATFS maps SD to "0:"
  
  // --- Configure MP4 Recording ---
  Serial.println("Step 1: Configuring MP4 recording parameters...");
  mp4.configVideo(configV);
  mp4.configAudio(configA, CODEC_AAC);
  mp4.setRecordingDuration(durationSeconds);
  mp4.setRecordingFileCount(1); // Record a single file segment
  mp4.setRecordingFileName(filename.c_str()); // Use the generated filename
  mp4.setRecordingDataType(STORAGE_ALL); // Ensure we record Audio and Video
  
  // --- Start Streaming Pipeline ---
  Serial.println("Step 2: Starting audio stream (Audio -> AAC)...");
  if (audioStreamer->begin() != 0) {
    Serial.println("[Error] Audio StreamIO link start failed");
    return false;
  }
  Serial.println("Audio stream started.");
  delay(300); // Increased delay after audio start

  Serial.println("Step 3: Starting Camera channel...");
  // Re-apply configuration before starting channel
  Camera.configVideoChannel(VIDEO_CHANNEL, configV);
  Camera.channelBegin(VIDEO_CHANNEL);
  Serial.println("Camera channel started.");
  delay(1000); // increased delay after camera channel starts
  
  // Check for camera buffer status
  Serial.println("Waiting for camera to fill buffer...");
  delay(500); // Additional delay to allow camera buffer to fill
  
  Serial.println("Step 4: Setting up and starting AV mix stream (Video + AAC -> MP4)...");
  // Register inputs/outputs just before starting the stream
  avMixStreamer->registerInput1(aac); 
  avMixStreamer->registerInput2(Camera.getStream(VIDEO_CHANNEL)); // Get stream AFTER channelBegin
  avMixStreamer->registerOutput(mp4);
  Serial.println("Attempting avMixStreamer->begin()...");
  if (avMixStreamer->begin() != 0) {
    Serial.println("[Error] AV Mix StreamIO link start failed");
    // Cleanup started components
    audioStreamer->end();
    Camera.channelEnd(VIDEO_CHANNEL);
    return false;
  }
  Serial.println("AV Mix stream started.");
  delay(300); // Increased delay after starting AV mixer
  
  // --- Start MP4 Recording ---
  Serial.println("Step 5: Starting MP4 recording...");
  mp4.begin(); // Start the actual recording process
  delay(500); // Increased delay to allow MP4 recording to initialize
  
  // --- Verify Recording State ---
  if (mp4.getRecordingState() != 1) { // Should be 1 (recording) after begin()
    int state = mp4.getRecordingState();
    Serial.print("[Error] MP4 recording failed to start (state=");
    Serial.print(state);
    Serial.println("). Expected state 1.");
    // Cleanup started components
    avMixStreamer->end();
    audioStreamer->end();
    Camera.channelEnd(VIDEO_CHANNEL);
    return false;
  }
  
  recording = true;
  recordingStartTime = millis();
  recordingDuration = durationMs;
  
  Serial.print("Recording started successfully. File: ");
  Serial.println(videoFilePath);
  
  // Print recording info
  printInfo();
  
  return true;
}

void VideoHandler::stopRecording() {
  if (!recording) {
    Serial.println("Not recording, stopRecording request ignored.");
    return;
  }
  
  Serial.println("==> stopRecording called. Stopping video recording...");
  
  // --- Stop MP4 Recording First ---
  int mp4State = mp4.getRecordingState();
  Serial.print("Current MP4 state before stopping: "); Serial.println(mp4State);
  if (mp4State == 1) { // Only end if currently recording (state 1)
    Serial.println("Stopping MP4 recording (mp4.end())...");
    mp4.end(); // This finalizes the file
    delay(1000); // Increased delay to ensure file is written/closed properly
  } else {
      Serial.print("MP4 not in recording state (state=");
      Serial.print(mp4State);
      Serial.println("), skipping mp4.end(). Might have auto-completed.");
  }

  // --- Stop Streaming Pipeline (Reverse Order of Start) ---
  Serial.println("Stopping AV mix stream (Video + AAC -> MP4)...");
  if (avMixStreamer != nullptr) {
    avMixStreamer->end();
  }
  delay(300); // Increased delay

  Serial.println("Stopping Camera channel...");
  Camera.channelEnd(VIDEO_CHANNEL);
  delay(500); // Increased delay after stopping camera channel

  Serial.println("Stopping audio stream (Audio -> AAC)...");
  if (audioStreamer != nullptr) {
    audioStreamer->end();
  }
  delay(300); // Increased delay
  
  // Additional clean up to ensure components are reset
  resetStreamConnections();
  delay(200);

  recording = false;
  recordingStartTime = 0; // Reset start time
  Serial.println("Recording stopped. File saved to SD card: " + videoFilePath);
}

String VideoHandler::getVideoFilePath() {
  return videoFilePath;
}

bool VideoHandler::isRecording() {
  return recording;
}

bool VideoHandler::shouldUploadRecording() {
  return uploadVideo;
}

void VideoHandler::update() {
  if (recording) {
    // Check if the mp4 recording state indicates it is stopped/completed
    int currentMp4State = mp4.getRecordingState();
    
    // Check if recording finished naturally (state 2)
    if (currentMp4State == 2) { 
      Serial.print("MP4 recording auto-completed (state=2). Duration likely reached.");
      Serial.println(" Stopping recording process...");
      stopRecording();
      return; 
    }
    
    // Check for error state (e.g., state 0 unexpectedly after starting)
    if (currentMp4State == 0 && recordingStartTime > 0 && millis() - recordingStartTime > 500) {
        Serial.println("[Warning] MP4 state is 0 while recording flag is true. Likely an error occurred. Forcing stop.");
        stopRecording();
        return;
    }

    // Check recording duration timeout (only if recording is still active)
    if (recordingDuration > 0 && (millis() - recordingStartTime >= recordingDuration)) {
      Serial.println("Recording duration reached, explicitly stopping recording...");
      stopRecording();
    }
  }
}

String VideoHandler::generateFilename() {
  const char chars[] = "abcdefghijklmnopqrstuvwxyz0123456789";
  char hash[6] = {0}; // 5 chars + null terminator
  
  randomSeed(millis() + analogRead(A0));
  
  // Generate random 5-character hash
  for (int i = 0; i < 5; i++) {
    hash[i] = chars[random(0, sizeof(chars) - 1)];
  }
  
  char filename[64];
  sprintf(filename, "%s_%s", baseFileName.c_str(), hash);
  return String(filename);
}

void VideoHandler::printInfo() {
  Serial.println("------------------------------");
  Serial.println("- Video Recording Information -");
  Serial.println("------------------------------");
  Camera.printInfo();
  
  Serial.println("- Audio Information -");
  audio.printInfo();
  
  Serial.println("- MP4 Recording Information -");
  mp4.printInfo();
} 