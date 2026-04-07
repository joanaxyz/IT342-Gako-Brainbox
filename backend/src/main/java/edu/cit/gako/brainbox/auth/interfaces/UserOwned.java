package edu.cit.gako.brainbox.auth.interfaces;

import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.exception.ForbiddenException;

public interface UserOwned {
    User getUser();

    default void assertOwnedBy(Long userId) {
        if (!getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this resource");
        }
    }
}
