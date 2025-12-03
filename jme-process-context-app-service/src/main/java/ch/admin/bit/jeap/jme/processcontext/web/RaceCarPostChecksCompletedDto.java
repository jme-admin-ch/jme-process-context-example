package ch.admin.bit.jeap.jme.processcontext.web;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;


@Value
@AllArgsConstructor
public class RaceCarPostChecksCompletedDto {
    List<String> carNumbers;
}
