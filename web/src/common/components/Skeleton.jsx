import '../styles/skeleton.css';

export const StudyCardSkeleton = () => (
  <div className="study-card" style={{ pointerEvents: 'none' }}>
    <div className="sc-body">
      <div className="sc-indicator-row">
        <span className="skel skel-dot" />
        <span className="skel skel-line" style={{ width: 72 }} />
      </div>
      <div className="skel skel-title" style={{ marginTop: 12, marginBottom: 8 }} />
      <div className="skel skel-line" style={{ width: 110 }} />
      <div className="skel skel-bar" style={{ marginTop: 14 }} />
    </div>
    <div className="sc-divider" />
    <div className="sc-footer">
      <div className="skel skel-btn" />
      <div className="skel skel-btn" />
    </div>
  </div>
);

export const LibRowSkeleton = () => (
  <div className="lib-row library-row" style={{ pointerEvents: 'none' }}>
    <div className="lib-row-name">
      <div className="skel skel-icon" />
      <div style={{ flex: 1 }}>
        <div className="skel skel-line" style={{ width: 160 }} />
        <div className="skel skel-line" style={{ width: 120, marginTop: 5 }} />
      </div>
    </div>
    <div className="lib-row-cell">
      <div className="skel skel-line" style={{ width: 140, height: 38, borderRadius: 10 }} />
    </div>
    <div className="lib-row-cell">
      <div className="skel skel-line" style={{ width: 52 }} />
    </div>
    <div className="lib-row-cell">
      <div className="skel skel-line" style={{ width: 90 }} />
    </div>
    <div className="lib-row-actions">
      <div className="skel skel-line" style={{ width: 64, height: 38, borderRadius: 10 }} />
    </div>
  </div>
);

export const TrackRowSkeleton = () => (
  <div className="pl-track-row" style={{ pointerEvents: 'none' }}>
    <span className="pl-track-num">
      <div className="skel" style={{ width: 16, height: 13, borderRadius: 3 }} />
    </span>
    <div className="pl-track-info">
      <div className="skel skel-line" style={{ width: 180 }} />
    </div>
    <span className="pl-track-cat">
      <div className="skel skel-line" style={{ width: 70 }} />
    </span>
    <span className="pl-track-secs">
      <div className="skel skel-line" style={{ width: 18 }} />
    </span>
    <div className="pl-track-acts" />
  </div>
);

export const PlaylistSidebarSkeleton = () => (
  <div className="pl-lib-item" style={{ pointerEvents: 'none' }}>
    <div className="skel" style={{ width: 40, height: 40, borderRadius: 6, flexShrink: 0 }} />
    <div className="pl-lib-info">
      <div className="skel skel-line" style={{ width: 100 }} />
      <div className="skel skel-line" style={{ width: 60, marginTop: 5 }} />
    </div>
  </div>
);
