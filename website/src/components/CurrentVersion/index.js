import useDocusaurusContext from '@docusaurus/useDocusaurusContext';

export default function CurrentVersion() {
  const { siteConfig } = useDocusaurusContext();

  return <div className="cstories-current-version">v{siteConfig.customFields.cstoriesVersion}</div>;
}
