package zerobase.stockinfo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zerobase.stockinfo.exception.impl.AlreadyExistUserException;
import zerobase.stockinfo.model.Auth;
import zerobase.stockinfo.model.MemberEntity;
import zerobase.stockinfo.persist.MemberRepository;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return this.memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("not found user ->"+username));
  }


  public MemberEntity register(Auth.SignUp member) {
    boolean exists = this.memberRepository.existsByUsername(member.getUsername());
    if (exists) {
      throw new AlreadyExistUserException();
    }
    member.setPassword(passwordEncoder.encode(member.getPassword()));

    return this.memberRepository.save(member.toEntity());
  }


  public MemberEntity authenticate(Auth.SignIn member) {
    var user = this.memberRepository.findByUsername(member.getUsername())
                                    .orElseThrow(() -> new UsernameNotFoundException("not found user ->" + member.getUsername()));

    if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
      throw new RuntimeException("비밀번호가 일치하지 않습니다");
    }

    return user;
  }


}
