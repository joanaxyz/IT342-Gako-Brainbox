import Modal from './Modal';

const ConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirm',
  message,
  confirmLabel = 'Confirm',
  variant = 'primary',
}) => (
  <Modal isOpen={isOpen} onClose={onClose} title={title}>
    <p>{message}</p>
    <div className="modal-actions">
      <button type="button" className="btn btn-ghost" onClick={onClose}>
        Cancel
      </button>
      <button type="button" className={`btn btn-${variant}`} onClick={onConfirm}>
        {confirmLabel}
      </button>
    </div>
  </Modal>
);

export default ConfirmModal;
