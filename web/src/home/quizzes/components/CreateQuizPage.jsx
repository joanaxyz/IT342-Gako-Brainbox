import { useQuiz } from '../../../notebook/shared/hooks/hooks';
import QuizComposerPage from './QuizComposerPage';

const CreateQuizPage = ({ onClose, notebooks = [] }) => {
  const { createQuiz } = useQuiz();

  return (
    <QuizComposerPage
      mode="create"
      notebooks={notebooks}
      onClose={onClose}
      onSubmit={(payload) => createQuiz(payload)}
    />
  );
};

export default CreateQuizPage;
