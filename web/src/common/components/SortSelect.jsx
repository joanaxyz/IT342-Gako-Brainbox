const SortSelect = ({ options, value, onChange }) => (
  <select
    className="ref-select"
    value={value}
    onChange={(e) => onChange(e.target.value)}
  >
    {options.map((o) => (
      <option key={o.value} value={o.value}>{o.label}</option>
    ))}
  </select>
);

export default SortSelect;
