import React from 'react';

const FieldInput = ({ 
  label, 
  type = 'text', 
  placeholder, 
  value, 
  onChange, 
  name,
  required = false,
  style = {}
}) => {
  return (
    <div className="field-group" style={style}>
      {label && <label className="field-label">{label}</label>}
      <input
        type={type}
        name={name}
        className="field-input"
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        required={required}
      />
    </div>
  );
};

export default FieldInput;
