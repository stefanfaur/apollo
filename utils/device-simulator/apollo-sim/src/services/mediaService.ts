import axios from 'axios';
import { API_CONFIG } from '../config';

export async function uploadMedia(file: File): Promise<string> {
  try {
    // Get presigned URL
    const presignedResponse = await axios.get(`${API_CONFIG.BASE_URL}/api/minio/presigned-url`, {
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
  const imageUrls = [
    'https://picsum.photos/1920/1080?random',
    'https://picsum.photos/1920/1080?random',
    'https://picsum.photos/1920/1080?random',
    'http://localhost:9000/apollo-assets/simulator-sample-images/sample1.jpg',
    'http://localhost:9000/apollo-assets/simulator-sample-images/sample2.jpg',
    'http://localhost:9000/apollo-assets/simulator-sample-images/sample3.jpg',
    'http://localhost:9000/apollo-assets/simulator-sample-images/sample4.jpg',
    'http://localhost:9000/apollo-assets/simulator-sample-images/sample5.jpeg',
    'http://localhost:9000/apollo-assets/simulator-sample-images/sample6.jpg',
  ];

  try {
    const imagePromises = imageUrls.map(async (url, index) => {
      const response = await fetch(url);
      const blob = await response.blob();
      return new File([blob], `sample-image-${index + 1}.jpg`, { type: 'image/jpeg' });
    });

    return Promise.all(imagePromises);
  } catch (error) {
    console.error('Error loading sample images:', error);
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
