import { RouterProvider } from 'react-router';
import { router } from './routes';
import { useBankStore } from '../store';

function App() {
  return (
    <div className="dark antialiased h-screen w-screen overflow-hidden bg-[#0A0A0F] text-white">
      <RouterProvider router={router} />
    </div>
  );
}

export default App;