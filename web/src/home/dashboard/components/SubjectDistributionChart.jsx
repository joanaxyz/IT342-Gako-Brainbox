import { useMemo } from 'react';

const SubjectDistributionChart = ({ notebookData = [], height = 200 }) => {
  const distributionData = useMemo(() => {
    // Generate sample subject distribution data
    const subjects = [
      { name: 'Mathematics', hours: 12, color: 'var(--accent)' },
      { name: 'Science', hours: 8, color: 'var(--ink)' },
      { name: 'History', hours: 6, color: 'var(--ink-2)' },
      { name: 'Literature', hours: 4, color: 'var(--ink-3)' },
      { name: 'Languages', hours: 3, color: 'var(--border-2)' }
    ];
    
    const totalHours = subjects.reduce((sum, subject) => sum + subject.hours, 0);
    
    return subjects.map(subject => ({
      ...subject,
      percentage: (subject.hours / totalHours) * 100
    }));
  }, []);

  const totalHours = distributionData.reduce((sum, subject) => sum + subject.hours, 0);

  return (
    <div className="subject-distribution-chart">
      <div className="chart-header">
        <div className="chart-title">Study Time by Subject</div>
        <div className="chart-stats">
          <span className="stat-item">
            <span className="stat-value">{totalHours}</span>
            <span className="stat-label">total hrs</span>
          </span>
        </div>
      </div>
      
      <div className="distribution-container" style={{ height: `${height}px` }}>
        <div className="horizontal-bars">
          {distributionData.map((subject, index) => (
            <div key={subject.name} className="subject-bar-row">
              <div className="subject-info">
                <div className="subject-color" style={{ backgroundColor: subject.color }}></div>
                <span className="subject-name">{subject.name}</span>
              </div>
              
              <div className="bar-container">
                <div className="bar-track">
                  <div 
                    className="bar-fill"
                    style={{ 
                      width: `${subject.percentage}%`,
                      backgroundColor: subject.color
                    }}
                  ></div>
                </div>
                <div className="bar-labels">
                  <span className="hours-label">{subject.hours}h</span>
                  <span className="percentage-label">{subject.percentage.toFixed(1)}%</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
      
      <div className="chart-footer">
        <div className="insight-text">
          Most studied: <strong>{distributionData[0]?.name}</strong> ({distributionData[0]?.hours}h)
        </div>
      </div>
    </div>
  );
};

export default SubjectDistributionChart;
