// Navbar.jsx
import { Link } from "react-router-dom";
import { Menu, ShoppingCart, User } from "lucide-react";

function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-left">
        <Menu className="icon" />
        <img src="/image.png" alt="Nollie Logo" className="logo" />
      </div>
      <div className="navbar-center">
        <Link to="/test">Furniture</Link>
        <Link to="/collection">Collection</Link>
      </div>
      <div className="navbar-right">
        <ShoppingCart className="icon" />
        <User className="icon" />
      </div>
    </nav>
  );
}
export default Navbar;