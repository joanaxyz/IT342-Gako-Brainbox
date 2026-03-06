import Modal from "../../../common/components/Modal";
import FieldInput from '../../../common/components/FieldInput';
import { useState } from 'react';
import { useCategory } from "../../../notebook/shared/hooks/hooks";

const NewCategoryModal = ({ isOpen, onClose }) => {
    const [categoryName, setCategoryName] = useState('');
    const { createCategory } = useCategory();

    const handleCreateCategory = async (e) => {
        e?.preventDefault();
        const name = categoryName.trim();
        if (name) {
            const response = await createCategory(name);
            if (response.success) {
                setCategoryName('');
                onClose();
            }
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="New category"
        >
            <form onSubmit={handleCreateCategory}>
                <FieldInput
                    label="Category name"
                    type="text"
                    placeholder="e.g. Computer Science"
                    value={categoryName}
                    onChange={(e) => setCategoryName(e.target.value)}
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

export default NewCategoryModal;
