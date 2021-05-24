
import http from '@/services/HttpClient.js'

class ExportService {

  exportRecords(input) {
    //var msg = Alerts.info('export.initiated');
    http.post('export-jobs', {}, input).then(
      function(savedJob) {
        // Alerts.remove(msg);
        if (savedJob.status == 'COMPLETED') {
          // Alerts.info('export.downloading_file');
          http.downloadFile(http.getUrl('export-jobs/') + savedJob.id + '/output');
        } else if (savedJob.status == 'FAILED') {
          // Alerts.error('export.failed', savedJob);
        } else {
          //Alerts.info('export.file_will_be_emailed', savedJob);
        }
      }
    );
  }

}

export default new ExportService();
