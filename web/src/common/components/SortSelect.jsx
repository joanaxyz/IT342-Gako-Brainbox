const SortSelect = ({ options, value, onChange, ariaLabel = 'Sort by', className = 'ref-select' }) => (
  <select
    aria-label={ariaLabel}
    className={className}
    value={value}
    onChange={(e) => onChange(e.target.value)}
  >
    {options.map((o) => (
      <option key={o.value} value={o.value}>{o.label}</option>
    ))}
  </select>
);

export default SortSelect;
