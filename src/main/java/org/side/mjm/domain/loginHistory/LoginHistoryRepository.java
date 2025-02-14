package org.side.mjm.domain.loginHistory;

import org.side.mjm.common.jpa.JpaDynamicRepository;
import org.side.mjm.entity.l_login;
import org.side.mjm.entity.key.l_login_key;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistoryRepository extends JpaDynamicRepository<l_login, l_login_key> {

}
