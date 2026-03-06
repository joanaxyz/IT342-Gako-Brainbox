import { createContextHook } from "../../../common/utils/createContextHook";
import { AuthContext } from "../contexts/AuthContext";

export const useAuth = createContextHook(
    AuthContext, 
    'useAuth', 
    'AuthProvider'
);
