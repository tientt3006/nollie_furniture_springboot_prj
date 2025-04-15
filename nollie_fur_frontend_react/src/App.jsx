import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import "./styles.css";

import AppRoutes from "./routes/routes.jsx"
import useAuth from "./hooks/authHook.jsx"
import AuthContext from "./provider/auth.context.jsx";
import CartService from "./apis/cart.service.jsx";
import CartContext from "./provider/cart.context.jsx";

function App() {
  const {user, toggleUser} = useAuth();
  const { cart, cartError, isProcessingCart, addItemToCart, removeItemFromCart, getCartInformation } = CartService();

  return (
    <Router>
      <AuthContext.Provider value={{user, toggleUser}}>
        <CartContext.Provider value={{ cart, cartError, isProcessingCart, addItemToCart, removeItemFromCart, getCartInformation }}>
          <AppRoutes/>
        </CartContext.Provider>
      </AuthContext.Provider>
    </Router>
  );
}

export default App;
