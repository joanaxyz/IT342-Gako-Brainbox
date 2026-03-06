import React, { createContext, useState, useCallback, useMemo } from 'react';

/**
 * Creates a reusable context factory for managing active states.
 * 
 * This factory returns a Context and Provider that manage a simple active/inactive state
 * with activate() and deactivate() methods. Perfect for modals, dropdowns, loading states, etc.
 * 
 * @returns {Object} An object containing { Context, Provider }
 * 
 * @example
 * // Create a context
 * const { Context: MyContext, Provider: MyProvider } = createActiveContext();
 * 
 * // Use in component
 * const { active, activate, deactivate } = useMyContext();
 * 
 * // Activate
 * activate();
 * 
 * // Deactivate
 * deactivate();
 */
export const createActiveContext = () => {
    // Create a new React Context
    const Context = createContext();

    // Create the Provider component
    const Provider = ({ children }) => {
        // State to track if the context is active
        const [active, setActive] = useState(false);

        // Function to activate (set to true)
        const activate = useCallback(() => {
            setActive(true);
        }, []);

        // Function to deactivate (set to false)
        const deactivate = useCallback(() => {
            setActive(false);
        }, []);

        // Memoize the context value to prevent unnecessary re-renders
        const value = useMemo(() => ({
            active,
            activate,
            deactivate
        }), [active, activate, deactivate]);

        // Provide the value to all children components
        return (
            <Context.Provider value={value}>
                {children}
            </Context.Provider>
        );
    };

    // Return both Context and Provider so they can be used separately
    return { Context, Provider };
};

