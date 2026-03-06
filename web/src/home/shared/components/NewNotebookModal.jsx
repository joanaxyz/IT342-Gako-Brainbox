import Modal from "../../../common/components/Modal";
import FieldInput from '../../../common/components/FieldInput';
import { useState } from 'react';
import { useNavigate } from "react-router-dom";
import { useNotebook } from "../../../notebook/shared/hooks/hooks";
import { useNotification } from "../../../common/hooks/hooks";
const NewNoteBookModal = ({ isOpen, onClose }) => {
    const navigate = useNavigate();
    const [notebookTitle, setNotebookTitle] = useState('');
    const { createNotebook } = useNotebook();
    const { addNotification } = useNotification();
    const handleCreateNotebook = async (e) => {
        e?.preventDefault();
        const title = notebookTitle.trim() || 'Untitled notebook';
        try {
            const response = await createNotebook({title});
            if (response.success) {
                onClose();
                navigate('/notebook/new', { state: { title } });
            } else {
                addNotification(response.message || 'Login failed. Please check your credentials.', 'error');
            }
        } catch (err) {
            console.error('Error:', err);
            addNotification('A network error occurred. Please try again.', 'error');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="New notebook"
        >
            <form onSubmit={handleCreateNotebook}>
                <FieldInput
                    label="Notebook title"
                    type="text"
                    placeholder="e.g. Introduction to Neuroscience"
                    value={notebookTitle}
                    onChange={(e) => setNotebookTitle(e.target.value)}
                />
                <div className="modal-actions">
                    <button type="button" className="btn btn-secondary" onClick={onClose}>
                        Cancel
                    </button>
                    <button type="submit" className="btn btn-primary">
                        Create
                    </button>
                </div>
            </form>
        </Modal>
    )
}

export default NewNoteBookModal;