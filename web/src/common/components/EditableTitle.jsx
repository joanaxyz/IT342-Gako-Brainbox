import React, { useState, useEffect } from 'react';

const EditableTitle = ({ initialTitle, onSave, className, tag: Tag = 'h2', readonly = false }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [title, setTitle] = useState(initialTitle);

  useEffect(() => {
    setTitle(initialTitle);
  }, [initialTitle]);

  const handleBlur = () => {
    setIsEditing(false);
    if (title !== initialTitle) {
      onSave(title);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.target.blur();
    }
    if (e.key === 'Escape') {
      setTitle(initialTitle);
      setIsEditing(false);
    }
  };

  if (isEditing && !readonly) {
    return React.createElement(
      Tag,
      { className },
      <input
        autoFocus
        className="editable-title-input"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onBlur={handleBlur}
        onKeyDown={handleKeyDown}
        onClick={(e) => e.stopPropagation()}
      />
    );
  }

  return React.createElement(
    Tag,
    {
      className,
      onDoubleClick: (e) => {
        if (readonly) return;
        e.stopPropagation();
        setIsEditing(true);
      },
    },
    title || 'Untitled'
  );
};

export default EditableTitle;
