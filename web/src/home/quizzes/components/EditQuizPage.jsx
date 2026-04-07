import { useQuiz } from '../../../notebook/shared/hooks/hooks';
import QuizComposerPage from './QuizComposerPage';

const EditQuizPage = ({ quiz, onClose, notebooks = [] }) => {
  const { updateQuiz, deleteQuiz } = useQuiz();

  return (
    <QuizComposerPage
      mode="edit"
      quiz={quiz}
      notebooks={notebooks}
      onClose={onClose}
      onSubmit={(payload) => updateQuiz(quiz.uuid, payload)}
      onDelete={deleteQuiz}
    />
  );
};

export default EditQuizPage;
