import { Outlet } from 'react-router-dom';
import '../editor.css';

const EditorLayout = () => {
  return (
    <div className="editor-layout">
      <Outlet />
    </div>
  );
};

export default EditorLayout;
