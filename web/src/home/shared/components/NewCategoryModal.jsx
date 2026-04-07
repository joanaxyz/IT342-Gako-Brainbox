import { useState } from 'react';
import Modal from "../../../common/components/Modal";
import FieldInput from '../../../common/components/FieldInput';
import { useNotification } from "../../../common/hooks/hooks";
import { useCategory } from "../../../notebook/shared/hooks/hooks";

const NewCategoryForm = ({ onClose, onCreated }) => {
    const [categoryName, setCategoryName] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { addNotification } = useNotification();
    const { createCategory } = useCategory();

    const handleCreateCategory = async (e) => {
        e?.preventDefault();
        const name = categoryName.trim();

        if (!name || isSubmitting) {
            return;
        }

        setIsSubmitting(true);
        const response = await createCategory(name);
        setIsSubmitting(false);

        if (!response.success) {
            addNotification(response.message || 'Failed to create category.', 'error');
            return;
        }

        setCategoryName('');

        if (onCreated) {
            onCreated(response.data);
            onClose();
            return;
        }

        onClose();
        addNotification(`Category "${response.data.name}" created.`, 'success', 2500);
    };

    return (
        <form onSubmit={handleCreateCategory}>
            <FieldInput
                label="Category name"
                type="text"
                placeholder="e.g. Computer Science"
                value={categoryName}
                onChange={(e) => setCategoryName(e.target.value)}
                required
            />
            <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose} disabled={isSubmitting}>
                    Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={!categoryName.trim() || isSubmitting}>
                    {isSubmitting ? 'Creating...' : 'Create'}
                </button>
            </div>
        </form>
    );
};

const NewCategoryModal = ({ isOpen, onClose, onCreated }) => {

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="New category"
        >
            {isOpen ? <NewCategoryForm onClose={onClose} onCreated={onCreated} /> : null}
        </Modal>
    )
}

export default NewCategoryModal;
