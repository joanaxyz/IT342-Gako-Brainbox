package edu.cit.gako.brainbox.platform.security.interfaces;

import edu.cit.gako.brainbox.modules.user.entity.User;
import edu.cit.gako.brainbox.shared.exception.ForbiddenException;

public interface UserOwned {
    User getUser();

    default void assertOwnedBy(Long userId) {
        if (!getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this resource");
        }
    }
}
