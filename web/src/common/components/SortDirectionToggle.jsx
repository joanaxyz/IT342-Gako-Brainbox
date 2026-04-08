import './sortDirectionToggle.css';

const DirectionIcon = ({ direction }) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M8 6v12" />
    <path d={direction === 'asc' ? 'm5 9 3-3 3 3' : 'm5 15 3 3 3-3'} />
    <path d="M16 6h3" />
    <path d="M16 12h3" />
    <path d="M16 18h3" />
  </svg>
);

const SortDirectionToggle = ({ direction, label, onToggle }) => (
  <button
    type="button"
    className="sort-direction-toggle"
    aria-label={`${label}. Currently ${direction === 'asc' ? 'ascending' : 'descending'}. Toggle sort direction.`}
    onClick={onToggle}
  >
    <DirectionIcon direction={direction} />
    {direction === 'asc' ? 'Asc' : 'Desc'}
  </button>
);

export default SortDirectionToggle;
