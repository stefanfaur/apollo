import axios from 'axios';
import { API_CONFIG } from '../config';

export async function uploadMedia(file: File): Promise<string> {
  try {
    // Get presigned URL
    const presignedResponse = await axios.get(`${API_CONFIG.BASE_URL}/api/files/presigned-url`, {
      params: {
        filename: file.name
      }
    });

    console.log('Presigned URL:', presignedResponse.data);

    const { url, objectName } = presignedResponse.data;

    // Upload file using presigned URL
    await axios.put(url, file, {
      headers: {
        'Content-Type': file.type,
      }
    }).then(() => {
        console.log('Media uploaded successfully at link:', url);
    });

    return objectName;
  } catch (error) {
    console.error('Error uploading media:', error);
    throw error;
  }
}

export async function loadSampleImages(): Promise<File[]> {
  // Use local video files from public/sample-videos directory
  const videoFiles = [
    'sample1.mp4',
    'sample2.mp4', 
    'sample3.mp4',
    'sample4.mp4',
    'sample5.mp4',
    'sample6.mp4',
    'sample7.mp4',
    'sample8.mp4',
    'sample9.mp4',
  ];

  try {
    const mediaPromises = videoFiles.map(async (filename, index) => {
      const url = `/sample-videos/${filename}`;
      try {
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error(`Failed to fetch ${filename}`);
        }
        const blob = await response.blob();
        return new File([blob], filename, { type: 'video/mp4' });
      } catch (error) {
        console.warn(`Failed to load ${filename}:`, error);
        // Create a minimal fallback file if the local file fails to load
        const fallbackBlob = new Blob(['sample video content'], { type: 'video/mp4' });
        return new File([fallbackBlob], filename, { type: 'video/mp4' });
      }
    });

    return Promise.all(mediaPromises);
  } catch (error) {
    console.error('Error loading sample videos:', error);
    return [];
  }
}

export async function loadSampleVideos(): Promise<File[]> {
  const videoUrls = [
    'https://example.com/sample-video-1.mp4',
    'https://example.com/sample-video-2.mp4'
  ];

  try {
    const videoPromises = videoUrls.map(async (url, index) => {
      const response = await fetch(url);
      const blob = await response.blob();
      return new File([blob], `sample-video-${index + 1}.mp4`, { type: 'video/mp4' });
    });

    return Promise.all(videoPromises);
  } catch (error) {
    console.error('Error loading sample videos:', error);
    return [];
  }
}
