package org.arcure.back.config.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("IsMyToken(#gameId, #tokenId)")
public @interface IsMyToken {
}
