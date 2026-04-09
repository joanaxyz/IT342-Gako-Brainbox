import React, { useEffect, useRef } from 'react';
import '../styles/loading_overlay.css';

const LOADER_DURATION_MS = 3000;

const LOADER_KEYFRAMES = [
  { progress: 0, rotationY: 45, rotationZ: 0, scaleX: 1, scaleY: 1, shadowScaleX: 1, shadowScaleY: 1, shadowOpacity: 0.46 },
  { progress: 0.25, rotationY: 135, rotationZ: 45, scaleX: 0.72, scaleY: 1.46, shadowScaleX: 0.68, shadowScaleY: 0.94, shadowOpacity: 0.22 },
  { progress: 0.5, rotationY: 225, rotationZ: 90, scaleX: 1, scaleY: 1, shadowScaleX: 1, shadowScaleY: 1, shadowOpacity: 0.46 },
  { progress: 0.75, rotationY: 315, rotationZ: 135, scaleX: 1.46, scaleY: 0.72, shadowScaleX: 1.38, shadowScaleY: 1.06, shadowOpacity: 0.68 },
  { progress: 1, rotationY: 405, rotationZ: 180, scaleX: 1, scaleY: 1, shadowScaleX: 1, shadowScaleY: 1, shadowOpacity: 0.46 },
];

const easeInOutCubic = (progress) => {
  if (progress < 0.5) {
    return 4 * progress * progress * progress;
  }

  return 1 - ((-2 * progress + 2) ** 3) / 2;
};

const interpolate = (start, end, progress) => start + (end - start) * progress;

const findStartKeyframeIndex = (progress) => {
  let index = 0;

  for (let i = 0; i < LOADER_KEYFRAMES.length; i += 1) {
    if (progress >= LOADER_KEYFRAMES[i].progress) {
      index = i;
    }
  }

  return Math.min(index, LOADER_KEYFRAMES.length - 2);
};

const getLoaderFrame = (progress) => {
  const clamped = Math.max(0, Math.min(1, progress));
  const startIndex = findStartKeyframeIndex(clamped);
  const start = LOADER_KEYFRAMES[startIndex];
  const end = LOADER_KEYFRAMES[startIndex + 1];
  const segmentSpan = Math.max(0.0001, end.progress - start.progress);
  const segmentProgress = Math.max(0, Math.min(1, (clamped - start.progress) / segmentSpan));
  const eased = easeInOutCubic(segmentProgress);

  return {
    rotationY: interpolate(start.rotationY, end.rotationY, eased),
    rotationZ: interpolate(start.rotationZ, end.rotationZ, eased),
    scaleX: interpolate(start.scaleX, end.scaleX, eased),
    scaleY: interpolate(start.scaleY, end.scaleY, eased),
    shadowScaleX: interpolate(start.shadowScaleX, end.shadowScaleX, eased),
    shadowScaleY: interpolate(start.shadowScaleY, end.shadowScaleY, eased),
    shadowOpacity: interpolate(start.shadowOpacity, end.shadowOpacity, eased),
  };
};

const applyLoaderFrame = (boxElement, shadowElement, frame) => {
  boxElement.style.transform = [
    'rotateX(-20deg)',
    `rotateY(${frame.rotationY}deg)`,
    `rotateZ(${frame.rotationZ}deg)`,
    `scaleX(${frame.scaleX})`,
    `scaleY(${frame.scaleY})`,
  ].join(' ');
  shadowElement.style.transform = `scaleX(${frame.shadowScaleX}) scaleY(${frame.shadowScaleY})`;
  shadowElement.style.opacity = String(frame.shadowOpacity);
};

const LoadingOverlay = ({ isActive }) => {
  const boxRef = useRef(null);
  const shadowRef = useRef(null);

  useEffect(() => {
    const boxElement = boxRef.current;
    const shadowElement = shadowRef.current;

    if (!boxElement || !shadowElement) {
      return undefined;
    }

    applyLoaderFrame(boxElement, shadowElement, getLoaderFrame(0));

    if (!isActive) {
      return undefined;
    }

    let animationFrameId = 0;
    let cycleStart = 0;

    const animate = (timestamp) => {
      if (cycleStart === 0) {
        cycleStart = timestamp;
      }

      const elapsed = (timestamp - cycleStart) % LOADER_DURATION_MS;
      const progress = elapsed / LOADER_DURATION_MS;

      applyLoaderFrame(boxElement, shadowElement, getLoaderFrame(progress));
      animationFrameId = window.requestAnimationFrame(animate);
    };

    animationFrameId = window.requestAnimationFrame(animate);

    return () => {
      window.cancelAnimationFrame(animationFrameId);
    };
  }, [isActive]);

  return (
    <div className={`loading-overlay ${isActive ? 'active' : ''}`}>
      <div className="running-box-container">
        <div ref={boxRef} className="logo-box">
          <div className="box-face top"></div>
          <div className="box-face left"></div>
          <div className="box-face right"></div>
          <div className="box-face back"></div>
          <div className="box-face bottom"></div>
          <div className="box-face left-back"></div>
        </div>
        <div ref={shadowRef} className="shadow"></div>
      </div>
    </div>
  );
};

export default LoadingOverlay;
