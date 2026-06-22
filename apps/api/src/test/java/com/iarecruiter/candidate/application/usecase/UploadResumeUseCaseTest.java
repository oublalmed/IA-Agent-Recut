package com.iarecruiter.candidate.application.usecase;

import com.iarecruiter.candidate.domain.model.Candidate;
import com.iarecruiter.candidate.domain.model.Resume;
import com.iarecruiter.candidate.domain.model.ResumeStatus;
import com.iarecruiter.candidate.domain.port.*;
import com.iarecruiter.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadResumeUseCaseTest {

    @Mock private CandidateRepository candidateRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private ResumeStoragePort resumeStoragePort;
    @Mock private ConsentLogRepository consentLogRepository;

    @InjectMocks private UploadResumeUseCase uploadResumeUseCase;

    private UUID companyId;
    private UUID userId;
    private Candidate existingCandidate;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        existingCandidate = Candidate.builder()
                .id(UUID.randomUUID()).companyId(companyId)
                .email("candidate@example.com")
                .erased(false).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    @Test
    void execute_uploadsResumeAndCreatesCandidate() throws Exception {
        MultipartFile file = new MockMultipartFile("cv.pdf", "cv.pdf", "application/pdf", new byte[1024]);
        when(candidateRepository.findByCompanyIdAndEmail(companyId, "candidate@example.com"))
                .thenReturn(Optional.empty());
        when(candidateRepository.save(any())).thenReturn(existingCandidate);
        when(resumeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(resumeStoragePort.upload(anyString(), any(), anyLong(), anyString())).thenReturn("key");

        var command = new UploadResumeUseCase.UploadCommand(
                companyId, userId, "candidate@example.com", "John", "Doe", file, "127.0.0.1");
        Resume result = uploadResumeUseCase.execute(command);

        assertThat(result.getStatus()).isEqualTo(ResumeStatus.UPLOADED);
        assertThat(result.getMimeType()).isEqualTo("application/pdf");
        verify(candidateRepository).save(any(Candidate.class));
        verify(resumeStoragePort).upload(anyString(), any(), anyLong(), eq("application/pdf"));
        verify(consentLogRepository).logConsent(any(), eq(companyId), eq("CV_PROCESSING"), eq(true), eq("127.0.0.1"));
    }

    @Test
    void execute_reusesExistingCandidate() throws Exception {
        MultipartFile file = new MockMultipartFile("cv.pdf", "cv.pdf", "application/pdf", new byte[1024]);
        when(candidateRepository.findByCompanyIdAndEmail(companyId, "candidate@example.com"))
                .thenReturn(Optional.of(existingCandidate));
        when(resumeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(resumeStoragePort.upload(anyString(), any(), anyLong(), anyString())).thenReturn("key");

        var command = new UploadResumeUseCase.UploadCommand(
                companyId, userId, "candidate@example.com", null, null, file, null);
        uploadResumeUseCase.execute(command);

        verify(candidateRepository, never()).save(any());
    }

    @Test
    void execute_rejectsInvalidFileType() {
        MultipartFile file = new MockMultipartFile("cv.txt", "cv.txt", "text/plain", new byte[100]);
        var command = new UploadResumeUseCase.UploadCommand(
                companyId, userId, "candidate@example.com", null, null, file, null);

        assertThatThrownBy(() -> uploadResumeUseCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    void execute_rejectsOversizedFile() {
        byte[] bigFile = new byte[11 * 1024 * 1024];
        MultipartFile file = new MockMultipartFile("cv.pdf", "cv.pdf", "application/pdf", bigFile);
        var command = new UploadResumeUseCase.UploadCommand(
                companyId, userId, "candidate@example.com", null, null, file, null);

        assertThatThrownBy(() -> uploadResumeUseCase.execute(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("too large");
    }
}
