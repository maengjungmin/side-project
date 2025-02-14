package org.side.mjm.domain.user;

import org.side.mjm.common.jpa.JpaDynamicRepository;
import org.side.mjm.entity.m_user;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaDynamicRepository<m_user, String> {

    Optional<m_user> findOneByUserIdAndAccessToken(String userId, String accessToken);
    Optional<m_user> findOneByUserIdAndRefreshToken(String userId, String refreshToken);

}
