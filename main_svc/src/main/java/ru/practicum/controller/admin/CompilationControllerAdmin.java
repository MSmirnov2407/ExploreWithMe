package ru.practicum.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/compilations")
@Slf4j
public class CompilationControllerAdmin {

    private final CompilationService compilationService;

    @Autowired
    public CompilationControllerAdmin(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto postCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        CompilationDto compilationDto = compilationService.create(newCompilationDto);
        log.info("Создана подборка событий id={}, title={}", compilationDto.getId(), compilationDto.getTitle());
        return compilationDto;
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable(name = "compId") int compId) {
        compilationService.deleteById(compId);
        log.info("Удалена подборка с id ={}", compId);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto patchCompilation(@PathVariable(name = "compId") int compId,
                                           @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        CompilationDto compilationDto = compilationService.update(compId, updateRequest);
        log.info("Обновлена подборка с id ={}", compId);
        return compilationDto;
    }
}
