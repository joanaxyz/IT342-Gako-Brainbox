import { useContext } from 'react';

export const createContextHook = (Context, hookName, providerName) => {
    return () => {
        const context = useContext(Context);

        if (!context) {
            throw new Error(`${hookName} must be used within ${providerName}`);
        }

        return context;
    };
};
