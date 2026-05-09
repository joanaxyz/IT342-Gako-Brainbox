import { useMemo } from 'react';

const ImprovedStudyChart = ({ type, data = [], height = 240 }) => {
  const chartData = useMemo(() => {
    if (type === 'activity') {
      // Use real activity data from recently reviewed notebooks
      const today = new Date();
      const days = [];
      
      for (let i = 29; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        
        // Count reviews on this specific day
        const dailyReviews = data.filter(n => 
          n.lastReviewedAt && new Date(n.lastReviewedAt).toDateString() === date.toDateString()
        ).length;
        
        const activity = dailyReviews;
        
        days.push({
          date: date.toISOString().split('T')[0],
          day: date.toLocaleDateString('en', { weekday: 'short' }).charAt(0),
          activity,
          level: activity === 0 ? 0 : activity <= 2 ? 1 : activity <= 4 ? 2 : 3
        });
      }
      
      return { days, totalActive: days.filter(d => d.activity > 0).length };
    }
    
    if (type === 'progress') {
      // Generate sample progress data for last 7 days
      const today = new Date();
      const days = [];
      
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today);
        date.setDate(date.getDate() - i);
        
        const quizScore = Math.floor(Math.random() * 30) + 70;
        const masteryLevel = Math.floor(Math.random() * 25) + 75;
        
        days.push({
          date: date.toISOString().split('T')[0],
          day: date.toLocaleDateString('en', { weekday: 'short' }),
          quizScore,
          masteryLevel
        });
      }
      
      return { days, latestQuiz: days[days.length - 1]?.quizScore || 0, latestMastery: days[days.length - 1]?.masteryLevel || 0 };
    }
    
    if (type === 'subjects') {
      // Use real notebook category data
      const categoryColors = {
        'Mathematics': '#C2410C',
        'Science': '#1C1917', 
        'History': '#57534E',
        'Literature': '#78716C',
        'Languages': '#A8A29E'
      };
      
      // Group notebooks by category
      const categoryGroups = data.reduce((groups, notebook) => {
        const category = notebook.categoryName || 'General';
        if (!groups[category]) {
          groups[category] = {
            name: category,
            count: 0,
            wordCount: 0
          };
        }
        groups[category].count += 1;
        groups[category].wordCount += notebook.wordCount || 0;
        return groups;
      }, {});
      
      const subjects = Object.values(categoryGroups).map(group => ({
        name: group.name,
        hours: group.wordCount, // Using wordCount as a proxy for study time
        color: categoryColors[group.name] || '#78716C'
      }));
      
      const totalHours = subjects.reduce((sum, subject) => sum + subject.hours, 0);
      
      return { 
        subjects: subjects.map(subject => ({
          ...subject,
          percentage: totalHours > 0 ? (subject.hours / totalHours) * 100 : 0
        })),
        totalHours
      };
    }
    
    return {};
  }, [type]);

  if (type === 'activity') {
    return (
      <div className="improved-chart activity-chart">
        <div className="chart-header">
          <div className="chart-title-group">
            <h3 className="chart-title">Study Activity</h3>
            <p className="chart-subtitle">Last 30 days</p>
          </div>
          <div className="chart-stats">
            <div className="stat">
              <span className="stat-value">{chartData.totalActive}</span>
              <span className="stat-label">active days</span>
            </div>
          </div>
        </div>
        
        <div className="activity-heatmap">
          <div className="heatmap-grid">
            {chartData.days.map((day, index) => (
              <div
                key={day.date}
                className={`heatmap-cell level-${day.level}`}
                title={`${day.date}: ${day.activity} sessions`}
              />
            ))}
          </div>
          
          <div className="heatmap-labels">
            <span>30 days ago</span>
            <span>Today</span>
          </div>
        </div>
        
        <div className="chart-legend">
          <div className="legend-item">
            <div className="legend-color level-0"></div>
            <span>No activity</span>
          </div>
          <div className="legend-item">
            <div className="legend-color level-1"></div>
            <span>Light</span>
          </div>
          <div className="legend-item">
            <div className="legend-color level-2"></div>
            <span>Moderate</span>
          </div>
          <div className="legend-item">
            <div className="legend-color level-3"></div>
            <span>Heavy</span>
          </div>
        </div>
      </div>
    );
  }

  if (type === 'progress') {
    const maxScore = 100;
    const chartWidth = 100;
    const chartHeight = height - 80;
    
    const createPath = (data, key) => {
      return data.map((point, index) => {
        const x = (index / (data.length - 1)) * chartWidth;
        const y = chartHeight - ((point[key] / maxScore) * chartHeight);
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      }).join(' ');
    };

    const quizPath = createPath(chartData.days, 'quizScore');
    const masteryPath = createPath(chartData.days, 'masteryLevel');

    return (
      <div className="improved-chart progress-chart">
        <div className="chart-header">
          <div className="chart-title-group">
            <h3 className="chart-title">Progress Trends</h3>
            <p className="chart-subtitle">Last 7 days performance</p>
          </div>
          <div className="chart-stats">
            <div className="stat">
              <span className="stat-value">{chartData.latestQuiz}%</span>
              <span className="stat-label">Quiz avg</span>
            </div>
            <div className="stat">
              <span className="stat-value">{chartData.latestMastery}%</span>
              <span className="stat-label">Mastery</span>
            </div>
          </div>
        </div>
        
        <div className="progress-chart-container">
          <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} className="progress-svg">
            {/* Simple grid lines */}
            {[25, 50, 75].map((value) => (
              <line
                key={value}
                x1="0"
                y1={chartHeight - (value / 100) * chartHeight}
                x2={chartWidth}
                y2={chartHeight - (value / 100) * chartHeight}
                stroke="#E5E0D8"
                strokeWidth="0.5"
                opacity="0.3"
              />
            ))}
            
            {/* Simple area fill for quiz only */}
            <defs>
              <linearGradient id="quizGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stopColor="#C2410C" stopOpacity="0.15" />
                <stop offset="100%" stopColor="#C2410C" stopOpacity="0.02" />
              </linearGradient>
            </defs>
            
            <path
              d={`${quizPath} L ${chartWidth} ${chartHeight} L 0 ${chartHeight} Z`}
              fill="url(#quizGradient)"
            />
            
            {/* Clean lines */}
            <path d={quizPath} fill="none" stroke="#C2410C" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
            <path d={masteryPath} fill="none" stroke="#1C1917" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" strokeDasharray="5,3" opacity="0.7" />
            
            {/* Simplified data points - only for quiz */}
            {chartData.days.map((point, index) => {
              const x = (index / (chartData.days.length - 1)) * chartWidth;
              const y = chartHeight - ((point.quizScore / maxScore) * chartHeight);
              return (
                <circle key={`quiz-${index}`} cx={x} cy={y} r="2.5" fill="#C2410C" stroke="#FFFFFF" strokeWidth="1.5" />
              );
            })}
          </svg>
          
          <div className="x-axis-labels">
            {chartData.days.map((day, index) => (
              <span key={day.date} className="x-label" style={{ left: `${(index / (chartData.days.length - 1)) * 100}%` }}>
                {day.day}
              </span>
            ))}
          </div>
        </div>
        
        <div className="chart-legend">
          <div className="legend-item">
            <div className="legend-line" style={{ backgroundColor: '#C2410C', height: '3px' }}></div>
            <span>Quiz Scores</span>
          </div>
          <div className="legend-item">
            <div className="legend-line" style={{ backgroundColor: '#1C1917', height: '2px', borderStyle: 'dashed', opacity: 0.7 }}></div>
            <span>Flashcard Mastery</span>
          </div>
        </div>
      </div>
    );
  }

  if (type === 'subjects') {
    return (
      <div className="improved-chart subjects-chart">
        <div className="chart-header">
          <div className="chart-title-group">
            <h3 className="chart-title">Study Time by Subject</h3>
            <p className="chart-subtitle">This month</p>
          </div>
          <div className="chart-stats">
            <div className="stat">
              <span className="stat-value">{chartData.totalHours}</span>
              <span className="stat-label">total hours</span>
            </div>
          </div>
        </div>
        
        <div className="subjects-list">
          {chartData.subjects.map((subject, index) => (
            <div key={subject.name} className="subject-item">
              <div className="subject-info">
                <div className="subject-color" style={{ backgroundColor: subject.color }}></div>
                <span className="subject-name">{subject.name}</span>
              </div>
              
              <div className="subject-bar">
                <div className="bar-track">
                  <div 
                    className="bar-fill"
                    style={{ 
                      width: `${subject.percentage}%`,
                      backgroundColor: subject.color
                    }}
                  ></div>
                </div>
                <div className="subject-labels">
                  <span className="hours">{subject.hours}h</span>
                  <span className="percentage">{subject.percentage.toFixed(0)}%</span>
                </div>
              </div>
            </div>
          ))}
        </div>
        
        <div className="chart-insight">
          <span className="insight-label">Most studied:</span>
          <span className="insight-value">{chartData.subjects[0]?.name}</span>
        </div>
      </div>
    );
  }

  return null;
};

export default ImprovedStudyChart;
