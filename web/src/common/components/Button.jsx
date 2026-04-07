import React from 'react';

const Button = ({ 
  children, 
  onClick, 
  type = 'button', 
  variant = 'primary', 
  fullWidth = false,
  disabled = false,
  style = {},
  className: extraClass = ''
}) => {
  const className = `btn btn-${variant} ${fullWidth ? 'btn-full' : ''} ${extraClass}`.trim();
  
  return (
    <button 
      type={type} 
      onClick={onClick} 
      className={className}
      style={style}
      disabled={disabled}
    >
      {children}
    </button>
  );
};

export default Button;
