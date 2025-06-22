#ifndef VIDEO_HANDLER_H
#define VIDEO_HANDLER_H

#include <Arduino.h>
#include <StreamIO.h>
#include <VideoStream.h>
#include <AudioStream.h>
#include <AudioEncoder.h>
#include <MP4Recording.h>
#include "AmebaFatFS.h"

class VideoHandler {
public:
  VideoHandler();
  ~VideoHandler();
  
  // Initialize the video recording components
  bool begin();
  
  // Clean up and release resources
  void end();
  
  // Start a video recording for the specified duration (in milliseconds)
  bool startRecording(unsigned long durationMs = 10000, bool shouldUpload = true);
  
  // Stop current video recording
  void stopRecording();
  
  // Get the path to the recorded video file
  String getVideoFilePath();
  
  // Check if recording is currently active
  bool isRecording();
  
  // Check if the current/last recording should be uploaded
  bool shouldUploadRecording();
  
  // Process video recording state in the main loop
  void update();

  AmebaFatFS fatfs;

private:
  // Helper for lifecycle management
  void createStreamers();
  void destroyStreamers();
  void resetStreamConnections();

  // Video and audio configuration
  static const int VIDEO_CHANNEL = 1;
  static const int AUDIO_PRESET = 0;
  
  VideoSetting configV;
  AudioSetting configA;
  Audio audio;
  AAC aac;
  MP4Recording mp4;
  
  // StreamIO objects, will be created/destroyed dynamically
  StreamIO* audioStreamer;    // Audio -> AAC
  StreamIO* avMixStreamer;    // Video + AAC -> MP4
  
  bool initialized;
  bool recording;
  bool uploadVideo;
  unsigned long recordingStartTime;
  unsigned long recordingDuration;
  String videoFilePath;
  String baseFileName;
  int fileCounter;
  
  // Generate unique filename for the video
  String generateFilename();
  
  // Print debug information
  void printInfo();
};

#endif // VIDEO_HANDLER_H