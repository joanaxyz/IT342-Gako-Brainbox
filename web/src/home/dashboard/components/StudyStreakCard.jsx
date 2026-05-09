import { useMemo } from 'react';
import { Book, Sparkles, Star, Zap, Flame } from 'lucide-react';

const StudyStreakCard = ({ reviewData = [], editData = [] }) => {
  const streakData = useMemo(() => {
    // Calculate current streak from review/edit timestamps
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    let currentStreak = 0;
    let longestStreak = 0;
    let tempStreak = 0;
    let totalActiveDays = 0;
    
    // Combine all activity data
    const allActivities = [
      ...reviewData.map(n => ({ date: n.lastReviewedAt, type: 'review' })),
      ...editData.map(n => ({ date: n.updatedAt, type: 'edit' }))
    ].filter(activity => activity.date);
    
    // Check last 30 days for activity
    for (let i = 0; i < 30; i++) {
      const checkDate = new Date(today);
      checkDate.setDate(checkDate.getDate() - i);
      
      // Check if there was any activity on this day
      const hasActivity = allActivities.some(activity => {
        const activityDate = new Date(activity.date);
        return activityDate.toDateString() === checkDate.toDateString();
      });
      
      if (hasActivity) {
        if (i === 0) currentStreak = 1;
        else if (i < currentStreak + 1) currentStreak++;
        
        tempStreak++;
        longestStreak = Math.max(longestStreak, tempStreak);
        totalActiveDays++;
      } else {
        tempStreak = 0;
        if (i === 0) currentStreak = 0;
      }
    }
    
    // Calculate consistency (percentage of active days)
    const consistency = totalActiveDays > 0 ? Math.round((totalActiveDays / 30) * 100) : 0;
    
    return {
      current: currentStreak,
      longest: longestStreak,
      totalDays: totalActiveDays,
      consistency
    };
  }, [reviewData, editData]);

  const getStreakIcon = (streak) => {
    const color = getStreakColor(streak);
    if (streak >= 30) {
      return <Flame size={24} color={color} />;
    }
    if (streak >= 14) {
      return <Zap size={24} color={color} />;
    }
    if (streak >= 7) {
      return <Star size={24} color={color} fill={color} />;
    }
    if (streak >= 3) {
      return <Sparkles size={24} color={color} />;
    }
    return <Book size={24} color={color} />;
  };

  const getStreakColor = (streak) => {
    if (streak >= 30) return 'var(--accent-dark)';
    if (streak >= 14) return 'var(--accent)';
    if (streak >= 7) return 'var(--accent-2)';
    if (streak >= 3) return 'var(--ink-2)';
    return 'var(--ink-3)';
  };

  return (
    <div className="study-streak-card">
      <div className="streak-header">
        <div className="streak-title">Study Streak</div>
        <div className="streak-icon-wrapper">{getStreakIcon(streakData.current)}</div>
      </div>
      
      <div className="streak-main">
        <div 
          className="streak-number"
          style={{ color: getStreakColor(streakData.current) }}
        >
          {streakData.current}
        </div>
        <div className="streak-label">days</div>
      </div>
      
      <div className="streak-stats">
        <div className="streak-stat">
          <span className="stat-value">{streakData.longest}</span>
          <span className="stat-label">longest</span>
        </div>
        <div className="streak-stat">
          <span className="stat-value">{streakData.totalDays}</span>
          <span className="stat-label">total</span>
        </div>
        <div className="streak-stat">
          <span className="stat-value">{streakData.consistency}%</span>
          <span className="stat-label">consistent</span>
        </div>
      </div>
      
      <div className="streak-progress">
        <div className="progress-label">Weekly Goal</div>
        <div className="progress-track">
          <div 
            className="progress-fill"
            style={{ 
              width: `${Math.min((streakData.current / 7) * 100, 100)}%`,
              backgroundColor: getStreakColor(streakData.current)
            }}
          ></div>
        </div>
        <div className="progress-text">
          {streakData.current}/7 days this week
        </div>
      </div>
    </div>
  );
};

export default StudyStreakCard;
