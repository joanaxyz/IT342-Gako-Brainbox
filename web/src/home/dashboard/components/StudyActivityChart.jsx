import { useMemo } from 'react';

const StudyActivityChart = ({ data = [], height = 120 }) => {
  const chartData = useMemo(() => {
    // Generate sample data for last 30 days
    const today = new Date();
    const days = [];
    
    for (let i = 29; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      
      // Simulate study activity (replace with real data)
      const activity = Math.random() > 0.3 ? Math.floor(Math.random() * 5) + 1 : 0;
      
      days.push({
        date: date.toISOString().split('T')[0],
        day: date.toLocaleDateString('en', { weekday: 'short' }).charAt(0),
        activity,
        level: activity === 0 ? 0 : activity <= 2 ? 1 : activity <= 4 ? 2 : 3
      });
    }
    
    return days;
  }, []);

  const maxActivity = Math.max(...chartData.map(d => d.activity));
  const totalActiveDays = chartData.filter(d => d.activity > 0).length;
  const currentStreak = calculateCurrentStreak(chartData);

  function calculateCurrentStreak(days) {
    let streak = 0;
    for (let i = days.length - 1; i >= 0; i--) {
      if (days[i].activity > 0) {
        streak++;
      } else {
        break;
      }
    }
    return streak;
  }

  const getLevelColor = (level) => {
    switch (level) {
      case 0: return 'var(--cream-3)';
      case 1: return 'var(--accent-bg)';
      case 2: return 'var(--accent)';
      case 3: return 'var(--accent-dark)';
      default: return 'var(--cream-3)';
    }
  };

  return (
    <div className="study-activity-chart">
      <div className="chart-header">
        <div className="chart-title">Study Activity</div>
        <div className="chart-stats">
          <span className="stat-item">
            <span className="stat-value">{totalActiveDays}</span>
            <span className="stat-label">days</span>
          </span>
          <span className="stat-item">
            <span className="stat-value">{currentStreak}</span>
            <span className="stat-label">streak</span>
          </span>
        </div>
      </div>
      
      <div className="heatmap-container" style={{ height: `${height}px` }}>
        <div className="heatmap-grid">
          {chartData.map((day, index) => (
            <div
              key={day.date}
              className="heatmap-cell"
              style={{
                backgroundColor: getLevelColor(day.level),
                opacity: day.level === 0 ? 1 : 0.8 + (day.level * 0.05),
                width: `${100 / 30}%`,
                height: '100%'
              }}
              title={`${day.date}: ${day.activity} study sessions`}
            />
          ))}
        </div>
        
        <div className="heatmap-labels">
          <span className="label-start">30 days ago</span>
          <span className="label-end">Today</span>
        </div>
      </div>
      
      <div className="chart-legend">
        <div className="legend-item">
          <div className="legend-color" style={{ backgroundColor: 'var(--cream-3)' }}></div>
          <span>No activity</span>
        </div>
        <div className="legend-item">
          <div className="legend-color" style={{ backgroundColor: 'var(--accent-bg)' }}></div>
          <span>Light</span>
        </div>
        <div className="legend-item">
          <div className="legend-color" style={{ backgroundColor: 'var(--accent)' }}></div>
          <span>Moderate</span>
        </div>
        <div className="legend-item">
          <div className="legend-color" style={{ backgroundColor: 'var(--accent-dark)' }}></div>
          <span>Heavy</span>
        </div>
      </div>
    </div>
  );
};

export default StudyActivityChart;
