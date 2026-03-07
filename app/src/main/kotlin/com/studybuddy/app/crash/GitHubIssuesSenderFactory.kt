package com.studybuddy.app.crash

import android.content.Context
import org.acra.config.CoreConfiguration
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

class GitHubIssuesSenderFactory : ReportSenderFactory {

    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return GitHubIssuesSender(
            context = context,
            repoOwner = CrashReportConfig.REPO_OWNER,
            repoName = CrashReportConfig.REPO_NAME,
            token = CrashReportConfig.getToken(context),
        )
    }

    override fun enabled(config: CoreConfiguration): Boolean = true
}
