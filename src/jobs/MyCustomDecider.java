package jobs;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class MyCustomDecider implements JobExecutionDecider {

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		// TODO Auto-generated method stub

		try {
			boolean someCondition = false;

	        if (someCondition) {
	            return FlowExecutionStatus.FAILED;
	        }
	        else {
	            return FlowExecutionStatus.COMPLETED;
	        }

		} catch(Throwable th) {

		}

		return null;
	}

}
