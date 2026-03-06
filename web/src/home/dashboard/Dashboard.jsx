import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/shared/hooks/useAuth';
import { useNotebook } from '../../notebook/shared/hooks/hooks';
import NewNoteBookModal from '../shared/components/NewNotebookModal';
import { getGreeting } from './Dashboard.utils';
import ReviewCard from './components/ReviewCard';
import NbCard from './components/NbCard';
import './styles/dashboard.css';

const Dashboard = () => {
  const { user } = useAuth();
  const name = user?.username || 'there';
  const { notebooks, setCurrentNotebook } = useNotebook();
  const navigate = useNavigate();
  const today = new Date();
  const dayName = today.toLocaleDateString('en-US', { weekday: 'long' });
  const monthDay = today.toLocaleDateString('en-US', { month: 'long', day: 'numeric' });

  const [showNewNotebookModal, setShowNewNotebookModal] = useState(false);

  const sortedNotebooks = [...notebooks].sort(
    (a, b) => new Date(b.updatedAt) - new Date(a.updatedAt)
  );

  const recentNotebooks = sortedNotebooks.slice(0, 3);
  const recentLibraryNotebooks = sortedNotebooks.slice(0, 6);

  return (
    <div className="page-body">
      <div className="dash-greeting">
        <div className="dash-greeting-label">
          {dayName}, {monthDay} · {getGreeting()}
        </div>
        <div className="dash-greeting-name">
          Ready to learn, <em>{name}?</em>
        </div>
        <div className="dash-greeting-sub">
          {notebooks.length > 0
            ? `You have ${notebooks.length} notebook${notebooks.length !== 1 ? 's' : ''}.`
            : 'Create a notebook to start learning.'}
        </div>
      </div>

      {recentNotebooks.length > 0 && (
        <>
          <div className="flex-between mb-12">
            <div className="section-label" style={{ margin: 0 }}>Continue learning</div>
            <button className="btn btn-ghost btn-xs" onClick={() => navigate('/library')}>
              See all
            </button>
          </div>
          <div className="reviewed-strip mb-36">
            {recentNotebooks.map((nb, i) => (
              <ReviewCard key={nb.id} notebook={nb} index={i} />
            ))}
          </div>
        </>
      )}

      <div className="flex-between mb-12">
        <div className="section-label" style={{ margin: 0 }}>Recently edited</div>
        <button className="btn btn-ghost btn-xs" onClick={() => navigate('/library')}>
          See all
        </button>
      </div>

      {recentLibraryNotebooks.length > 0 ? (
        <div className="nb-grid">
          {recentLibraryNotebooks.map((nb, i) => (
            <NbCard key={nb.id} notebook={nb} index={i} setCurrentNotebook={setCurrentNotebook}/>
          ))}
        </div>
      ) : (
        <p className="dash-empty">No notebooks yet. <button className="dash-empty-link" onClick={() => setShowNewNotebookModal(true)}>Create one</button> to start learning.</p>
      )}

      <NewNoteBookModal isOpen={showNewNotebookModal} onClose={() => setShowNewNotebookModal(false)} />
    </div>
  );
};

export default Dashboard;
