package com.ssafy.backend.api.service;

import com.ssafy.backend.api.request.CompanyInfoPostReq;
import com.ssafy.backend.api.request.CompanyRegisterPostReq;
import com.ssafy.backend.db.entity.Company;
import com.ssafy.backend.db.entity.CompanyInfo;
import com.ssafy.backend.db.entity.User;
import com.ssafy.backend.db.repository.CompanyInfoRepository;
import com.ssafy.backend.db.repository.CompanyRepository;
import com.ssafy.backend.db.repository.CompanyRepositorySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

/**
 *	기업 관련 비즈니스 로직 처리를 위한 서비스 구현 정의.
 */
@Service("companyService")
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService{

    private final CompanyRepository companyRepository;

    private final CompanyRepositorySupport companyRepositorySupport;

    private final CompanyInfoRepository companyInfoRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Company createCompany(CompanyRegisterPostReq companyRegisterInfo) {
        // 닉네임 중복 확인
        validateDuplicateMember(companyRegisterInfo);
        // 이메일 중복 확인
        validateDuplicateEmail(companyRegisterInfo);
        // 기업명 중복 확인
        validateDuplicateName(companyRegisterInfo);

        Company company = new Company();
        company.setUsername(companyRegisterInfo.getUsername());
        // 보안을 위해서 유저 패스워드 암호화 하여 디비에 저장.
        company.setPassword(passwordEncoder.encode(companyRegisterInfo.getPassword()));
        company.setEmail(companyRegisterInfo.getEmail());
        company.setName(companyRegisterInfo.getName());
        company.setPhone(companyRegisterInfo.getPhone());

        return companyRepository.save(company);
    }

    @Override
    public void validateDuplicateMember(CompanyRegisterPostReq companyRegisterInfo) {
        if (getCompanyByUsername(companyRegisterInfo.getUsername()).orElse(null) != null) {
            throw new IllegalStateException("Please enter a different username.");
        }
    }

    @Override
    public void validateDuplicateEmail(CompanyRegisterPostReq companyRegisterInfo) {
        if (getCompanyByEmail(companyRegisterInfo.getEmail()).orElse(null) != null) {
            throw new IllegalStateException("중복된 이메일입니다.");
        }
    }

    @Override
    public void validateDuplicateName(CompanyRegisterPostReq companyRegisterInfo) {
        if (getCompanyByName(companyRegisterInfo.getName()).orElse(null) != null) {
            throw new IllegalStateException("이미 존재하는 기업명입니다.");
        }
    }


    @Override
    public Optional<Company> getCompanyByEmail(String email) {
        Optional<Company> company = companyRepository.findCompanyByEmail(email);
        return company;
    }

    @Override
    public Optional<Company> getCompanyByName(String name) {
        Optional<Company> company = companyRepository.findCompanyByName(name);
        return company;
    }

    @Override
    public Optional<Company> getCompanyByUsername(String username) {
        // 디비에 유저 정보 조회 (userId 를 통한 조회).
        Optional<Company> company = companyRepositorySupport.findCompanyByUsername(username);
        return company;
    }

    @Override
    public Optional<Company> getCompanyByCompanyId(Long companyId) {
        return companyRepository.findById(companyId);
    }

    @Override
    public Optional<CompanyInfo> getCompanyInfoByCompanyId(Long companyId) {
        Optional<CompanyInfo> companyInfo = companyInfoRepository.findCompanyInfoByCompanyId(companyId);
        return companyInfo;
    }

    @Override
    public CompanyInfo createCompanyInfo(Company company, CompanyInfoPostReq companyInfoPostReq) {
        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setCompany(company);
        companyInfo.setCeo(companyInfoPostReq.getCeo());
        companyInfo.setAddress(companyInfoPostReq.getAddress());
        companyInfo.setCompanyCall(companyInfoPostReq.getCompanyCall());
        companyInfo.setRegistrationNumber(companyInfoPostReq.getRegistrationNumber());
        companyInfo.setRegistrationFile(companyInfoPostReq.getRegistrationFile());
        return companyInfoRepository.save(companyInfo);
    }

    @Override
    public CompanyInfo updateCompanyInfo(Long companyId, Map<Object, Object> objectMap) {
        CompanyInfo companyInfo = companyInfoRepository.findCompanyInfoByCompanyId(companyId).get();
        objectMap.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(CompanyInfo.class, (String) key);
            field.setAccessible(true);
            ReflectionUtils.setField(field, companyInfo, value);
        });
        return companyInfoRepository.save(companyInfo);
    }

    @Override
    public Company updateCompany(Long companyId, Map<Object, Object> objectMap) {
        Company company = companyRepository.findById(companyId).get();
        objectMap.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Company.class, (String) key);
            field.setAccessible(true);
            ReflectionUtils.setField(field, company, value);
        });
        return companyRepository.save(company);
    }
}
