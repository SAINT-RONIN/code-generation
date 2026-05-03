import { Outlet } from 'react-router';

export default function AppLayout() {
  return (
    <div className="h-full w-full">
      <Outlet />
    </div>
  );
}