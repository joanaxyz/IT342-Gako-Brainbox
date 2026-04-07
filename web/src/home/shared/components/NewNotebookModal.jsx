import { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";
import Modal from "../../../common/components/Modal";
import FieldInput from '../../../common/components/FieldInput';
import { useNotebook, useCategory } from "../../../notebook/shared/hooks/hooks";
import { useNotification } from "../../../common/hooks/hooks";

const NewNotebookForm = ({ onClose, initialCategoryId = null }) => {
    const navigate = useNavigate();
    const [notebookTitle, setNotebookTitle] = useState('');
    const [selectedCategoryId, setSelectedCategoryId] = useState(() => (
        initialCategoryId ? String(initialCategoryId) : ''
    ));
    const { createNotebook } = useNotebook();
    const { categories } = useCategory();
    const { addNotification } = useNotification();

    const handleCreateNotebook = async (e) => {
        e?.preventDefault();
        const title = notebookTitle.trim() || 'Untitled notebook';
        const payload = { title };

        if (selectedCategoryId) {
            payload.categoryId = Number(selectedCategoryId);
        }

        try {
            const response = await createNotebook(payload);

            if (response.success) {
                onClose();
                setNotebookTitle('');
                setSelectedCategoryId('');
                navigate(`/notebook/${response.data.uuid}`, { state: { title } });
            } else {
                addNotification(response.message || 'Failed to create notebook.', 'error');
            }
        } catch (err) {
            console.error('Error:', err);
            addNotification('A network error occurred. Please try again.', 'error');
        }
    };

    return (
        <form onSubmit={handleCreateNotebook}>
            <FieldInput
                label="Notebook title"
                type="text"
                placeholder="e.g. Introduction to Neuroscience"
                value={notebookTitle}
                onChange={(e) => setNotebookTitle(e.target.value)}
            />
            <div className="field-group">
                <label className="field-label">Category</label>
                <select
                    className="field-select"
                    value={selectedCategoryId}
                    onChange={(e) => setSelectedCategoryId(e.target.value)}
                >
                    <option value="">None (Uncategorized)</option>
                    {categories.map((cat) => (
                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                    ))}
                </select>
            </div>
            <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={onClose}>
                    Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                    Create
                </button>
            </div>
        </form>
    );
};

const NewNoteBookModal = ({ isOpen, onClose, initialCategoryId = null }) => {
    const { fetchCategories } = useCategory();

    useEffect(() => {
        if (isOpen) {
            fetchCategories(false);
        }
    }, [fetchCategories, isOpen]);

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="New notebook"
        >
            {isOpen ? (
                <NewNotebookForm
                    key={initialCategoryId ?? 'none'}
                    onClose={onClose}
                    initialCategoryId={initialCategoryId}
                />
            ) : null}
        </Modal>
    )
}

export default NewNoteBookModal;
