import { useFlashcard } from '../../../notebook/shared/hooks/hooks';
import DeckComposerPage from './DeckComposerPage';

const CreateDeckPage = ({ onClose, notebooks = [] }) => {
  const { createFlashcard } = useFlashcard();

  return (
    <DeckComposerPage
      mode="create"
      notebooks={notebooks}
      onClose={onClose}
      onSubmit={(payload) => createFlashcard(payload)}
    />
  );
};

export default CreateDeckPage;
