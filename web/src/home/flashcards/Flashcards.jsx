import { useState, useMemo } from 'react';
import FlashcardPlayer from './FlashcardPlayer';
import CreateDeckModal from './components/CreateDeckModal';
import { useNotebook } from '../../notebook/shared/hooks/hooks';
import SortSelect from '../../common/components/SortSelect';
import '../dashboard/styles/dashboard.css';

const INITIAL_DECKS = [
  {
    id: 1,
    title: 'Introduction to Computer Science',
    cardCount: 15,
    lastReviewed: '2 days ago',
    mastery: 70,
    notebookId: null,
    notebookTitle: null,
    cards: [
      { front: 'What is an Algorithm?', back: 'A step-by-step procedure for solving a problem.' },
      { front: 'What is a Variable?', back: 'A storage location paired with an associated symbolic name.' },
      { front: 'What is Recursion?', back: 'A method where the solution depends on smaller instances of the same problem.' },
    ],
  },
  {
    id: 2,
    title: 'Biology Basics',
    cardCount: 24,
    lastReviewed: 'Yesterday',
    mastery: 45,
    notebookId: null,
    notebookTitle: null,
    cards: [
      { front: 'What is the powerhouse of the cell?', back: 'Mitochondria' },
      { front: 'What is Photosynthesis?', back: 'The process by which plants use sunlight to synthesize food.' },
    ],
  },
  {
    id: 3,
    title: 'Organic Chemistry Reactions',
    cardCount: 18,
    lastReviewed: '1 week ago',
    mastery: 20,
    notebookId: null,
    notebookTitle: null,
    cards: [
      { front: 'What is a substitution reaction?', back: 'A reaction where one atom or group is replaced by another.' },
    ],
  },
  {
    id: 4,
    title: 'World History – Cold War',
    cardCount: 12,
    lastReviewed: '3 days ago',
    mastery: 55,
    notebookId: null,
    notebookTitle: null,
    cards: [
      { front: 'When did the Cold War start?', back: 'Circa 1947, following the end of World War II.' },
    ],
  },
];

const SORT_OPTIONS = [
  { value: 'recent', label: 'Most recent' },
  { value: 'az', label: 'A – Z' },
  { value: 'za', label: 'Z – A' },
  { value: 'most', label: 'Most cards' },
  { value: 'least', label: 'Fewest cards' },
];

const ICON_VARIANTS = ['sky', 'green', 'rose', 'violet', 'amber'];
const ICON_COLORS = { sky: 'stone', green: 'accent', rose: 'accent', violet: 'cream', amber: 'stone' };

const getMasteryClass = (pct) => pct >= 60 ? 'good' : pct >= 35 ? 'mid' : 'low';
const getFillClass = (pct) => pct >= 60 ? 'progress-fill-ink' : pct >= 35 ? 'progress-fill-stone' : 'progress-fill-accent';

const ArrowRight = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
    <line x1="5" y1="12" x2="19" y2="12"/>
    <polyline points="12 5 19 12 12 19"/>
  </svg>
);

const NotebookIcon = () => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
  </svg>
);

const DeckCard = ({ deck, index, onStudy }) => {
  const variant = ICON_VARIANTS[index % ICON_VARIANTS.length];
  const iconBg = ICON_COLORS[variant] || 'stone';
  const mastery = deck.mastery ?? 0;

  return (
    <div className="study-card">
      <div className="sc-top">
        <div className={`sc-icon ${iconBg}`}>🗂️</div>
        <div className="sc-info">
          <div className="sc-stats">
            <span className="sc-stat">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="4" width="20" height="16" rx="2"/>
                <path d="M7 4v16"/>
              </svg>
              {deck.cardCount} cards
            </span>
          </div>
          <div className="sc-title">{deck.title}</div>
          {deck.notebookTitle && (
            <span className="sc-notebook-badge">
              <NotebookIcon />
              {deck.notebookTitle}
            </span>
          )}
        </div>
      </div>
      <div className="sc-body">
        <div className="sc-progress-row">
          <span className="sc-progress-label">Mastery</span>
          <span className={`sc-progress-val ${getMasteryClass(mastery)}`}>{mastery}%</span>
        </div>
        <div className="progress-track-sm">
          <div className={getFillClass(mastery)} style={{ width: `${mastery}%` }} />
        </div>
        <div className="score-row" style={{ marginTop: 2 }}>
          <span className="muted text-xs">Last studied</span>
          <span className="score-val text-xs">{deck.lastReviewed}</span>
        </div>
      </div>
      <div className="sc-footer">
        <button
          className={`sc-action-btn${mastery >= 60 ? ' primary' : ''}`}
          onClick={() => onStudy(deck)}
        >
          Study deck
          <ArrowRight />
        </button>
      </div>
    </div>
  );
};

const Flashcards = () => {
  const [decks, setDecks] = useState(INITIAL_DECKS);
  const [activeDeck, setActiveDeck] = useState(null);
  const [search, setSearch] = useState('');
  const [sortBy, setSortBy] = useState('recent');
  const [selectedNotebookId, setSelectedNotebookId] = useState('all');
  const [showCreate, setShowCreate] = useState(false);
  const { notebooks } = useNotebook();

  const notebookPills = useMemo(() => {
    const ids = new Set(decks.filter((d) => d.notebookId).map((d) => String(d.notebookId)));
    const linkedNotebooks = notebooks.filter((n) => ids.has(String(n.id)));
    const hasStandalone = decks.some((d) => !d.notebookId);
    return { linkedNotebooks, hasStandalone };
  }, [decks, notebooks]);

  const filtered = useMemo(() => {
    let result = [...decks];
    if (search.trim()) {
      const q = search.toLowerCase();
      result = result.filter((d) => d.title.toLowerCase().includes(q));
    }
    if (selectedNotebookId === 'standalone') {
      result = result.filter((d) => !d.notebookId);
    } else if (selectedNotebookId !== 'all') {
      result = result.filter((d) => String(d.notebookId) === String(selectedNotebookId));
    }
    if (sortBy === 'az') result.sort((a, b) => a.title.localeCompare(b.title));
    else if (sortBy === 'za') result.sort((a, b) => b.title.localeCompare(a.title));
    else if (sortBy === 'most') result.sort((a, b) => b.cardCount - a.cardCount);
    else if (sortBy === 'least') result.sort((a, b) => a.cardCount - b.cardCount);
    return result;
  }, [decks, search, sortBy, selectedNotebookId]);

  const grouped = useMemo(() => {
    if (selectedNotebookId !== 'all') return null;
    const notebookMap = new Map();
    const standalone = [];
    filtered.forEach((deck) => {
      if (deck.notebookId) {
        const key = String(deck.notebookId);
        if (!notebookMap.has(key)) {
          const nb = notebooks.find((n) => String(n.id) === key);
          notebookMap.set(key, {
            notebook: nb || { id: deck.notebookId, title: deck.notebookTitle || 'Notebook' },
            items: [],
          });
        }
        notebookMap.get(key).items.push(deck);
      } else {
        standalone.push(deck);
      }
    });
    return { groups: [...notebookMap.values()], standalone };
  }, [filtered, notebooks, selectedNotebookId]);

  if (activeDeck) {
    return <FlashcardPlayer deck={activeDeck} onExit={() => setActiveDeck(null)} />;
  }

  const isEmpty = filtered.length === 0;

  return (
    <div className="page-body">
      <div className="flex-between mb-28">
        <div>
          <div className="page-title">Flashcards</div>
          <div className="page-subtitle">Active recall decks for long-term retention</div>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          New deck
        </button>
      </div>

      <div className="study-controls-bar">
        <div className="input-wrap" style={{ flex: 1, maxWidth: 280 }}>
          <span className="input-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
          </span>
          <input
            type="search"
            className="search-input-field"
            placeholder="Search decks…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <SortSelect options={SORT_OPTIONS} value={sortBy} onChange={setSortBy} />
      </div>

      <div className="pill-row mb-20">
        <button
          className={`pill${selectedNotebookId === 'all' ? ' active' : ''}`}
          onClick={() => setSelectedNotebookId('all')}
        >
          All
        </button>
        {notebookPills.linkedNotebooks.map((nb) => (
          <button
            key={nb.id}
            className={`pill${selectedNotebookId === String(nb.id) ? ' active' : ''}`}
            onClick={() => setSelectedNotebookId(String(nb.id))}
          >
            {nb.title}
          </button>
        ))}
        {notebookPills.hasStandalone && (
          <button
            className={`pill${selectedNotebookId === 'standalone' ? ' active' : ''}`}
            onClick={() => setSelectedNotebookId('standalone')}
          >
            Standalone
          </button>
        )}
      </div>

      {isEmpty ? (
        <div className="study-empty-state">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="study-empty-icon">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="M7 4v16"/>
          </svg>
          <p className="study-empty-title">No decks found</p>
          <p className="study-empty-desc">
            {search ? `No results for "${search}"` : 'Create your first flashcard deck to get started.'}
          </p>
          {!search && (
            <button className="btn btn-primary btn-sm" onClick={() => setShowCreate(true)}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              New deck
            </button>
          )}
        </div>
      ) : grouped ? (
        <>
          {grouped.groups.map((group, gi) => (
            <div key={group.notebook.id} className="study-group">
              <div className="study-group-header">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
                </svg>
                <span className="study-group-title">{group.notebook.title}</span>
                {group.notebook.categoryName && (
                  <span className="chip chip-neutral">{group.notebook.categoryName}</span>
                )}
                <span className="study-group-count">{group.items.length} deck{group.items.length !== 1 ? 's' : ''}</span>
              </div>
              <div className="study-grid">
                {group.items.map((deck, i) => (
                  <DeckCard key={deck.id} deck={deck} index={gi * 10 + i} onStudy={setActiveDeck} />
                ))}
              </div>
            </div>
          ))}
          {grouped.standalone.length > 0 && (
            <div className="study-group">
              <div className="study-group-header">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="3" width="18" height="18" rx="2"/>
                  <path d="M3 9h18"/>
                </svg>
                <span className="study-group-title">Standalone</span>
                <span className="study-group-count">{grouped.standalone.length} deck{grouped.standalone.length !== 1 ? 's' : ''}</span>
              </div>
              <div className="study-grid">
                {grouped.standalone.map((deck, i) => (
                  <DeckCard key={deck.id} deck={deck} index={i} onStudy={setActiveDeck} />
                ))}
              </div>
            </div>
          )}
        </>
      ) : (
        <>
          <div className="section-label">{filtered.length} deck{filtered.length !== 1 ? 's' : ''}</div>
          <div className="study-grid">
            {filtered.map((deck, i) => (
              <DeckCard key={deck.id} deck={deck} index={i} onStudy={setActiveDeck} />
            ))}
          </div>
        </>
      )}

      <CreateDeckModal
        isOpen={showCreate}
        onClose={() => setShowCreate(false)}
        onCreate={(d) => setDecks((prev) => [d, ...prev])}
        notebooks={notebooks}
      />
    </div>
  );
};

export default Flashcards;
