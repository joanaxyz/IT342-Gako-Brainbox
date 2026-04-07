import { createContextHook } from "../../../common/utils/createContextHook";
import { AuthContext } from "../contexts/AuthContextValue";

export const useAuth = createContextHook(
    AuthContext, 
    'useAuth', 
    'AuthProvider'
);
