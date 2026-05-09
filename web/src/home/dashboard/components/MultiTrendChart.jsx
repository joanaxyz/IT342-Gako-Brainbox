import { useMemo } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area, BarChart, Bar } from 'recharts';

const MultiTrendChart = ({ trendType = 'performance', quizData = [], flashcardData = [], notebooks = [], recentlyReviewed = [], recentlyEdited = [], height = 320 }) => {
  const chartData = useMemo(() => {
    const today = new Date();
    const days = [];
    
    for (let i = 6; i >= 0; i--) {
      const date = new Date(today);
      date.setDate(date.getDate() - i);
      
      let dayData = {
        date: date.toISOString().split('T')[0],
        day: date.toLocaleDateString('en', { weekday: 'short' }).slice(0, 2),
      };

      switch (trendType) {
        case 'quiz-performance':
          // Use real quiz scores and attempts data
          const quizScores = quizData
            .filter(q => q.bestScore !== null && q.bestScore !== undefined)
            .map(q => q.bestScore);
          const quizAttempts = quizData
            .filter(q => q.attempts > 0)
            .reduce((sum, q) => sum + q.attempts, 0);
          
          dayData.quizScore = quizScores.length > 0 
            ? Math.round(quizScores.reduce((a, b) => a + b, 0) / quizScores.length)
            : 0;
          dayData.quizAttempts = quizAttempts;
          break;
          
        case 'flashcard-mastery':
          // Use real flashcard mastery and attempts data
          const masteryLevels = flashcardData
            .filter(f => f.bestMastery !== null && f.bestMastery !== undefined)
            .map(f => f.bestMastery);
          const flashcardAttempts = flashcardData
            .filter(f => f.attempts > 0)
            .reduce((sum, f) => sum + f.attempts, 0);
          
          dayData.masteryLevel = masteryLevels.length > 0
            ? Math.round(masteryLevels.reduce((a, b) => a + b, 0) / masteryLevels.length)
            : 0;
          dayData.flashcardAttempts = flashcardAttempts;
          break;
          
        case 'review-activity':
          // Use real notebook review data
          const reviewCount = recentlyReviewed.filter(n => 
            n.lastReviewedAt && new Date(n.lastReviewedAt).toDateString() === date.toDateString()
          ).length;
          
          dayData.reviews = reviewCount;
          break;
          
        case 'edit-activity':
          // Use real notebook edit data
          const editCount = recentlyEdited.filter(n =>
            n.updatedAt && new Date(n.updatedAt).toDateString() === date.toDateString()
          ).length;
          
          dayData.edits = editCount;
          break;
          
        default:
          break;
      }
      
      days.push(dayData);
    }
    
    return days;
  }, [trendType, quizData, flashcardData, notebooks, recentlyReviewed, recentlyEdited]);

  const getTrendConfig = () => {
    switch (trendType) {
      case 'quiz-performance':
        return {
          title: 'Quiz Performance',
          subtitle: 'Quiz scores & attempts',
          type: 'area',
          lines: [
            { dataKey: 'quizScore', name: 'Quiz Score', color: '#C2410C' },
            { dataKey: 'quizAttempts', name: 'Attempts', color: '#1C1917' }
          ],
          domain: [0, 100]
        };
        
      case 'flashcard-mastery':
        return {
          title: 'Flashcard Mastery',
          subtitle: 'Mastery levels & attempts',
          type: 'area',
          lines: [
            { dataKey: 'masteryLevel', name: 'Mastery Level', color: '#C2410C' },
            { dataKey: 'flashcardAttempts', name: 'Attempts', color: '#1C1917' }
          ],
          domain: [0, 100]
        };
        
      case 'review-activity':
        return {
          title: 'Review Activity',
          subtitle: 'Notebook reviews',
          type: 'bar',
          lines: [
            { dataKey: 'reviews', name: 'Reviews', color: '#C2410C' }
          ],
          domain: [0, 'auto']
        };
        
      case 'edit-activity':
        return {
          title: 'Edit Activity',
          subtitle: 'Notebook edits',
          type: 'bar',
          lines: [
            { dataKey: 'edits', name: 'Edits', color: '#C2410C' }
          ],
          domain: [0, 'auto']
        };
        
      default:
        return { title: 'Trend', subtitle: '', type: 'line', lines: [], domain: [0, 100] };
    }
  };

  const config = getTrendConfig();
  const latestValue = chartData[chartData.length - 1]?.[config.lines[0]?.dataKey] || 0;
  const averageValue = Math.round(chartData.reduce((sum, day) => sum + (day[config.lines[0]?.dataKey] || 0), 0) / chartData.length);

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="progress-tooltip">
          <div className="tooltip-date">{label}</div>
          {payload.map((entry, index) => (
            <div key={index} className="tooltip-item" style={{ color: entry.color }}>
              <span className="tooltip-name">{entry.name}: </span>
              <span className="tooltip-value">
                {(trendType === 'quiz-performance' || trendType === 'flashcard-mastery') && entry.dataKey.includes('Score') || entry.dataKey.includes('Level') 
                  ? `${Math.round(entry.value)}%` 
                  : `${entry.value}`}
              </span>
            </div>
          ))}
        </div>
      );
    }
    return null;
  };

  const renderChart = () => {
    const commonProps = {
      data: chartData,
      margin: { top: 10, right: 10, left: 0, bottom: 0 }
    };

    switch (config.type) {
      case 'area':
        return (
          <AreaChart {...commonProps}>
            <defs>
              {config.lines.map((line, index) => (
                <linearGradient key={line.dataKey} id={`${line.dataKey}Gradient`} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={line.color} stopOpacity={0.3}/>
                  <stop offset="95%" stopColor={line.color} stopOpacity={0.05}/>
                </linearGradient>
              ))}
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#E5E0D8" opacity={0.3} vertical={false} />
            <XAxis dataKey="day" tick={{ fontSize: 11, fill: '#78716C' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 11, fill: '#78716C' }} axisLine={false} tickLine={false} domain={config.domain} />
            <Tooltip content={<CustomTooltip />} />
            {config.lines.map((line) => (
              <Area
                key={line.dataKey}
                type="monotone"
                dataKey={line.dataKey}
                stroke={line.color}
                strokeWidth={2.5}
                fill={`url(#${line.dataKey}Gradient)`}
                name={line.name}
              />
            ))}
          </AreaChart>
        );
        
      case 'bar':
        return (
          <BarChart {...commonProps}>
            <CartesianGrid strokeDasharray="3 3" stroke="#E5E0D8" opacity={0.3} vertical={false} />
            <XAxis dataKey="day" tick={{ fontSize: 11, fill: '#78716C' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 11, fill: '#78716C' }} axisLine={false} tickLine={false} domain={config.domain} />
            <Tooltip content={<CustomTooltip />} />
            {config.lines.map((line, index) => (
              <Bar
                key={line.dataKey}
                dataKey={line.dataKey}
                fill={line.color}
                name={line.name}
                stackId="stack"
              />
            ))}
          </BarChart>
        );
        
      case 'line':
        return (
          <LineChart {...commonProps}>
            <CartesianGrid strokeDasharray="3 3" stroke="#E5E0D8" opacity={0.3} vertical={false} />
            <XAxis dataKey="day" tick={{ fontSize: 11, fill: '#78716C' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 11, fill: '#78716C' }} axisLine={false} tickLine={false} domain={config.domain} />
            <Tooltip content={<CustomTooltip />} />
            {config.lines.map((line) => (
              <Line
                key={line.dataKey}
                type="monotone"
                dataKey={line.dataKey}
                stroke={line.color}
                strokeWidth={2.5}
                dot={{ r: 3 }}
                name={line.name}
              />
            ))}
          </LineChart>
        );
        
      default:
        return null;
    }
  };

  return (
    <div className="improved-chart progress-chart">
      <div className="chart-header">
        <div className="chart-title-group">
          <h3 className="chart-title">{config.title}</h3>
          <p className="chart-subtitle">{config.subtitle}</p>
        </div>
        <div className="chart-stats">
          <div className="stat">
            <span className="stat-value">
              {(trendType === 'quiz-performance' || trendType === 'flashcard-mastery') && config.lines[0]?.dataKey.includes('Score') || config.lines[0]?.dataKey.includes('Level')
                ? `${Math.round(latestValue)}%`
                : `${latestValue}`}
            </span>
            <span className="stat-label">Current</span>
          </div>
          <div className="stat">
            <span className="stat-value">
              {(trendType === 'quiz-performance' || trendType === 'flashcard-mastery') && config.lines[0]?.dataKey.includes('Score') || config.lines[0]?.dataKey.includes('Level')
                ? `${Math.round(averageValue)}%`
                : `${averageValue}`}
            </span>
            <span className="stat-label">Average</span>
          </div>
        </div>
      </div>
      
      <div className="progress-chart-container">
        <ResponsiveContainer width="100%" height={height - 80}>
          {renderChart()}
        </ResponsiveContainer>
      </div>
      
      <div className="chart-legend">
        {config.lines.map((line) => (
          <div key={line.dataKey} className="legend-item">
            <div className="legend-rect" style={{ backgroundColor: line.color }}></div>
            <span>{line.name}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MultiTrendChart;
