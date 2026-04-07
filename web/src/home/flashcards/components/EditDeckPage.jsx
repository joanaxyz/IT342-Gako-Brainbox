import { useFlashcard } from '../../../notebook/shared/hooks/hooks';
import DeckComposerPage from './DeckComposerPage';

const EditDeckPage = ({ deck, onClose, notebooks = [] }) => {
  const { updateFlashcard, deleteFlashcard } = useFlashcard();

  return (
    <DeckComposerPage
      mode="edit"
      deck={deck}
      notebooks={notebooks}
      onClose={onClose}
      onSubmit={(payload) => updateFlashcard(deck.uuid, payload)}
      onDelete={deleteFlashcard}
    />
  );
};

export default EditDeckPage;
