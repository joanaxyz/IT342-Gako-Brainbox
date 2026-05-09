import { useMemo } from 'react';

const ProgressTrendChart = ({ quizData = [], flashcardData = [], height = 180 }) => {
  const chartData = useMemo(() => {
    // Generate sample progress data for last 7 days
    const today = new Date();
    const days = [];
    
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      
      // Simulate progress data (replace with real data)
      const quizScore = Math.floor(Math.random() * 30) + 70; // 70-100%
      const masteryLevel = Math.floor(Math.random() * 25) + 75; // 75-100%
      
      days.push({
        date: date.toISOString().split('T')[0],
        day: date.toLocaleDateString('en', { weekday: 'short' }),
        quizScore,
        masteryLevel
      });
    }
    
    return days;
  }, []);

  const maxScore = 100;
  const chartWidth = 100;
  const chartHeight = height - 40; // Account for labels
  
  // Create paths for the lines
  const createPath = (data, key) => {
    return data.map((point, index) => {
      const x = (index / (data.length - 1)) * chartWidth;
      const y = chartHeight - ((point[key] / maxScore) * chartHeight);
      return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
    }).join(' ');
  };

  const quizPath = createPath(chartData, 'quizScore');
  const masteryPath = createPath(chartData, 'masteryLevel');

  // Calculate area under the curve
  const createAreaPath = (data, key) => {
    const linePath = createPath(data, key);
    return `${linePath} L ${chartWidth} ${chartHeight} L 0 ${chartHeight} Z`;
  };

  const quizArea = createAreaPath(chartData, 'quizScore');
  const masteryArea = createAreaPath(chartData, 'masteryLevel');

  const latestQuizScore = chartData[chartData.length - 1]?.quizScore || 0;
  const latestMastery = chartData[chartData.length - 1]?.masteryLevel || 0;

  return (
    <div className="progress-trend-chart">
      <div className="chart-header">
        <div className="chart-title">Progress Trends</div>
        <div className="chart-stats">
          <span className="stat-item">
            <span className="stat-value">{latestQuizScore}%</span>
            <span className="stat-label">Quiz</span>
          </span>
          <span className="stat-item">
            <span className="stat-value">{latestMastery}%</span>
            <span className="stat-label">Mastery</span>
          </span>
        </div>
      </div>
      
      <div className="chart-container" style={{ height: `${height}px` }}>
        <svg
          viewBox={`0 0 ${chartWidth} ${chartHeight}`}
          className="progress-svg"
          preserveAspectRatio="none"
        >
          {/* Grid lines */}
          {[0, 25, 50, 75, 100].map((value) => (
            <line
              key={value}
              x1="0"
              y1={chartHeight - (value / 100) * chartHeight}
              x2={chartWidth}
              y2={chartHeight - (value / 100) * chartHeight}
              stroke="var(--border)"
              strokeWidth="0.5"
              opacity="0.3"
            />
          ))}
          
          {/* Area fills */}
          <path
            d={quizArea}
            fill="var(--accent)"
            opacity="0.1"
          />
          <path
            d={masteryArea}
            fill="var(--ink)"
            opacity="0.05"
          />
          
          {/* Lines */}
          <path
            d={quizPath}
            fill="none"
            stroke="var(--accent)"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
          <path
            d={masteryPath}
            fill="none"
            stroke="var(--ink)"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeDasharray="4,2"
          />
          
          {/* Data points */}
          {chartData.map((point, index) => {
            const x = (index / (chartData.length - 1)) * chartWidth;
            const y = chartHeight - ((point.quizScore / maxScore) * chartHeight);
            return (
              <circle
                key={`quiz-${index}`}
                cx={x}
                cy={y}
                r="3"
                fill="var(--accent)"
                stroke="var(--white)"
                strokeWidth="1"
              />
            );
          })}
          
          {chartData.map((point, index) => {
            const x = (index / (chartData.length - 1)) * chartWidth;
            const y = chartHeight - ((point.masteryLevel / maxScore) * chartHeight);
            return (
              <circle
                key={`mastery-${index}`}
                cx={x}
                cy={y}
                r="2"
                fill="var(--ink)"
                stroke="var(--white)"
                strokeWidth="1"
              />
            );
          })}
        </svg>
        
        <div className="chart-labels">
          {chartData.map((day, index) => (
            <span
              key={day.date}
              className="x-label"
              style={{ left: `${(index / (chartData.length - 1)) * 100}%` }}
            >
              {day.day.charAt(0)}
            </span>
          ))}
        </div>
      </div>
      
      <div className="chart-legend">
        <div className="legend-item">
          <div className="legend-line" style={{ backgroundColor: 'var(--accent)' }}></div>
          <span>Quiz Scores</span>
        </div>
        <div className="legend-item">
          <div className="legend-line" style={{ backgroundColor: 'var(--ink)', borderStyle: 'dashed' }}></div>
          <span>Flashcard Mastery</span>
        </div>
      </div>
    </div>
  );
};

export default ProgressTrendChart;
