import { useEffect, useState } from "react";


const useAuth = () => {
    const [user, setUser] = useState()

    const toggleUser = () => {
        const user = JSON.parse(localStorage.getItem("user"))
        setUser(user)
    }

    useEffect(() => {
        toggleUser()
        console.log("toggling user")
    }, [])

    return {user, toggleUser}
}

export default useAuth;