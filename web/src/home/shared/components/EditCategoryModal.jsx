import { useEffect, useRef, useState } from 'react';
import Modal from '../../../common/components/Modal';
import FieldInput from '../../../common/components/FieldInput';
import { useNotification } from '../../../common/hooks/hooks';
import { useCategory } from '../../../notebook/shared/hooks/hooks';

const EditCategoryForm = ({ category, onClose }) => {
  const [categoryName, setCategoryName] = useState(category?.name || '');
  const [isSaving, setIsSaving] = useState(false);
  const isMountedRef = useRef(true);
  const { addNotification } = useNotification();
  const { updateCategory } = useCategory();

  useEffect(() => () => {
    isMountedRef.current = false;
  }, []);

  const handleUpdateCategory = async (event) => {
    event?.preventDefault();
    const name = categoryName.trim();

    if (isSaving || !category || !name || name === category.name) {
      return;
    }

    setIsSaving(true);
    const response = await updateCategory(category.id, name, false);

    if (isMountedRef.current) {
      setIsSaving(false);
    }

    if (!response.success) {
      addNotification(response.message || 'Failed to rename category.', 'error');
      return;
    }

    onClose();
    addNotification(`Category renamed to "${response.data.name}".`, 'success', 2500);
  };

  const trimmedName = categoryName.trim();
  const canSubmit = Boolean(category && trimmedName && trimmedName !== category.name && !isSaving);

  return (
    <form onSubmit={handleUpdateCategory}>
      <FieldInput
        label="Category name"
        type="text"
        placeholder="e.g. Computer Science"
        value={categoryName}
        onChange={(event) => setCategoryName(event.target.value)}
        required
      />
      <div className="modal-actions">
        <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSaving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={!canSubmit}>
          {isSaving ? 'Saving...' : 'Save'}
        </button>
      </div>
    </form>
  );
};

const EditCategoryModal = ({ category, isOpen, onClose }) => (
  <Modal
    isOpen={isOpen}
    onClose={onClose}
    title="Edit category"
  >
    {isOpen ? <EditCategoryForm key={category?.id} category={category} onClose={onClose} /> : null}
  </Modal>
);

export default EditCategoryModal;
