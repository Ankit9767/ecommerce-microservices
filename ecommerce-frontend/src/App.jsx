import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import Layout from "./components/Layout";
import Home from "./pages/Home";
import NotFound from "./pages/NotFound";

function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />

        <Route path="/home" element={<Navigate to="/" replace />} />

        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
}

export default App;