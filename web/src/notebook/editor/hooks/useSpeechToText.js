import { useCallback, useEffect, useRef, useState } from 'react';
import { aiAPI } from '../../../common/utils/api.jsx';

const PICKED_MIME_TYPES = [
  'audio/webm;codecs=opus',
  'audio/webm',
  'audio/mp4',
];
const MAX_TRANSCRIPTION_BYTES = 25 * 1024 * 1024;

const resolveMimeType = () => {
  if (typeof MediaRecorder === 'undefined' || typeof MediaRecorder.isTypeSupported !== 'function') {
    return '';
  }

  return PICKED_MIME_TYPES.find((mimeType) => MediaRecorder.isTypeSupported(mimeType)) || '';
};

const extensionFromMimeType = (mimeType) => {
  if (mimeType.includes('mp4')) {
    return 'm4a';
  }

  return 'webm';
};

export const useSpeechToText = ({
  enabled = true,
  onTranscript,
  onError,
}) => {
  const mediaRecorderRef = useRef(null);
  const mediaStreamRef = useRef(null);
  const chunksRef = useRef([]);
  const [status, setStatus] = useState('idle');
  const [recordingSeconds, setRecordingSeconds] = useState(0);

  const stopTracks = useCallback(() => {
    mediaStreamRef.current?.getTracks?.().forEach((track) => {
      track.stop();
    });
    mediaStreamRef.current = null;
  }, []);

  useEffect(() => () => {
    const recorder = mediaRecorderRef.current;

    if (recorder && recorder.state !== 'inactive') {
      recorder.stop();
    }

    stopTracks();
  }, [stopTracks]);

  useEffect(() => {
    if (status !== 'recording') {
      setRecordingSeconds(0);
      return undefined;
    }

    const startedAt = Date.now();
    const intervalId = window.setInterval(() => {
      setRecordingSeconds(Math.floor((Date.now() - startedAt) / 1000));
    }, 250);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [status]);

  const startRecording = useCallback(async () => {
    if (!enabled) {
      return;
    }

    if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === 'undefined') {
      onError?.('Microphone recording is not supported in this browser.');
      return;
    }

    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mimeType = resolveMimeType();
      const recorder = mimeType
        ? new MediaRecorder(stream, { mimeType })
        : new MediaRecorder(stream);

      mediaStreamRef.current = stream;
      mediaRecorderRef.current = recorder;
      chunksRef.current = [];

      recorder.addEventListener('dataavailable', (event) => {
        if (event.data && event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      });

      recorder.addEventListener('stop', async () => {
        try {
          const audioBlob = new Blob(chunksRef.current, {
            type: recorder.mimeType || mimeType || 'audio/webm',
          });

          if (audioBlob.size === 0) {
            setStatus('idle');
            onError?.('No audio was captured. Please try again.');
            return;
          }

          if (audioBlob.size > MAX_TRANSCRIPTION_BYTES) {
            throw new Error('Recording is too large for Groq transcription. Keep it under 25 MB.');
          }

          const extension = extensionFromMimeType(audioBlob.type);
          const file = new File([audioBlob], `brainbox-transcription-${Date.now()}.${extension}`, {
            type: audioBlob.type || 'audio/webm',
          });
          const response = await aiAPI.transcribeAudio(file);

          if (!response.success || !response.data?.text?.trim()) {
            throw new Error(response.message || 'Transcription failed.');
          }

          onTranscript?.(response.data.text.trim());
        } catch (error) {
          onError?.(error.message || 'Unable to transcribe the recording.');
        } finally {
          chunksRef.current = [];
          setStatus('idle');
          stopTracks();
        }
      });

      recorder.start();
      setStatus('recording');
    } catch (error) {
      setStatus('idle');
      stopTracks();
      onError?.(error.message || 'Unable to access the microphone.');
    }
  }, [enabled, onError, onTranscript, stopTracks]);

  const stopRecording = useCallback(() => {
    const recorder = mediaRecorderRef.current;

    if (!recorder || recorder.state === 'inactive') {
      setStatus('idle');
      return;
    }

    setStatus('transcribing');
    recorder.stop();
  }, []);

  const toggleRecording = useCallback(() => {
    if (status === 'recording') {
      stopRecording();
      return;
    }

    if (status === 'transcribing') {
      return;
    }

    void startRecording();
  }, [startRecording, status, stopRecording]);

  return {
    status,
    recordingSeconds,
    isRecording: status === 'recording',
    isTranscribing: status === 'transcribing',
    toggleRecording,
    stopRecording,
  };
};

export default useSpeechToText;
